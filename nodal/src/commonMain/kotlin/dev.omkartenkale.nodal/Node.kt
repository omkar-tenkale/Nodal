package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.exceptions.DisallowedNodeAdditionException
import dev.omkartenkale.nodal.exceptions.NodeCreationException
import dev.omkartenkale.nodal.lifecycle.ChildrenUpdatedEvent
import dev.omkartenkale.nodal.misc.BackPressHandler
import dev.omkartenkale.nodal.misc.RemovalRequest
import dev.omkartenkale.nodal.misc.instantiate
import dev.omkartenkale.nodal.plugin.NodalPlugin
import dev.omkartenkale.nodal.plugin.NodalPlugins
import dev.omkartenkale.nodal.plugin.NodeAddedEvent
import dev.omkartenkale.nodal.plugin.NodeCreatedEvent
import dev.omkartenkale.nodal.plugin.NodeRemovedEvent
import dev.omkartenkale.nodal.plugin.default.NodeTreeVisualiser
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

internal val nodeId = atomic(0)

public open class Node {

    private lateinit var _scope: Scope
    public val dependencies: Scope
        get() = if (::_scope.isInitialized) {
            _scope
        } else error("Accessing dependencies in init block is not supported, Wrap with doOnInit{ .. }")

    private val onInitBlocks = mutableListOf<() -> Unit>()
    public fun doOnInit(block: () -> Unit) {
        if (::_scope.isInitialized) {
            block()
        } else {
            onInitBlocks += block
        }
    }

    private val _isAddedEvents: MutableStateFlow<Boolean> = MutableStateFlow(false)
    public val isAddedEvents: StateFlow<Boolean> = _isAddedEvents

    public val plugins: NodalPlugins by dependencies<NodalPlugins>()
    public var isDead: Boolean = false
        private set

    public open val providesDependencies: DependencyDeclaration = {}
    private val _childrenUpdatedEvents: MutableSharedFlow<ChildrenUpdatedEvent> =
        MutableSharedFlow()
    public val childrenUpdatedEvents: SharedFlow<ChildrenUpdatedEvent> = _childrenUpdatedEvents

    private fun init(scope: Scope) {
        _scope = scope
    }

    private val _children = mutableListOf<Node>()
    public val children: List<Node> = _children

    public var coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    public inline fun <reified T : Node> addChild(noinline dependencyDeclaration: DependencyDeclaration = {}): T =
        addChild(T::class, dependencyDeclaration) as T

    public fun addChild(
        klass: KClass<out Node>, dependencyDeclaration: DependencyDeclaration
    ): Node {
        if (isDead) {
            throw DisallowedNodeAdditionException(this::class, klass)
        }
        val mergedDependencyDeclaration: DependencyDeclaration = {
            providesSelf<RemovalRequest> {
                RemovalRequest {
                    removeChild(it)
                }
            }
            include(dependencyDeclaration)
        }
        val node = instantiateNode(klass, dependencies, mergedDependencyDeclaration)
        plugins.all.forEach { it.onEvent(NodeCreatedEvent(this, node)) }
        _children.add(node)
        node.dispatchAdded()
        coroutineScope.launch {
            _childrenUpdatedEvents.emit(ChildrenUpdatedEvent.NodeAdded(node))
            plugins.all.forEach { it.onEvent(NodeAddedEvent(this@Node, node)) }
        }
        return node
    }

    protected fun removeChild(node: Node) {
        if (children.contains(node)) {
            node.dispatchRemoved()
            _children.remove(node)
            coroutineScope.launch {
                _childrenUpdatedEvents.emit(ChildrenUpdatedEvent.NodeRemoved(node))
                plugins.all.forEach { it.onEvent(NodeRemovedEvent(this@Node, node)) }
            }
        } else error("Cant remove node as parent does not contain child. \n\tParent: ${this::class.simpleName} (${this::class.qualifiedName}), \n\tChild: ${node::class.simpleName} (${node::class.qualifiedName})")
    }

    public open fun handleDetachRequest(node: Node): Boolean {
        removeChild(node)
        return true
    }

    public open fun onAdded() {}
    internal fun dispatchAdded() {
        coroutineScope.launch {
            onAdded()
            _isAddedEvents.emit(true)
        }
    }

    public open fun onRemoved() {}
    internal fun dispatchRemoved() {
        coroutineScope.launch(Dispatchers.Unconfined) {
            onRemoved()
            _isAddedEvents.emit(false)
            yield()
            children.forEach { it.dispatchRemoved() }
            coroutineScope.cancel()
            dependencies.close()
        }
        isDead = true
    }

    public fun removeSelf(): Unit = dependencies.get<RemovalRequest>().invoke(this)

    public companion object {
        public inline fun <reified T : Any> Node.dependencies(): Lazy<T> = lazy {
            dependencies.get<T>()
        }.also {
            doOnInit {
                if (dependencies.get<NodalConfig>().createEagerInstances) {
                    it.value
                }
            }
        }

        public val Node.ui: UI get() = dependencies.get<UI>()
        public val Node.backPressHandler: BackPressHandler get() = dependencies.get<BackPressHandler>()
        private fun instantiateNode(
            klass: KClass<out Node>,
            parentScope: Scope,
            dependencyDeclaration: DependencyDeclaration,
        ): Node = try {
            val newNodeId = nodeId.getAndIncrement()
            val nodeScope = Scope("${klass.simpleName}#$newNodeId", parentScope)
            klass.instantiate().also {
                it.init(nodeScope)
                DependencyDeclarationDSL(nodeScope).apply(dependencyDeclaration)
                    .apply(it.providesDependencies)
                it.onInitBlocks.forEach { it.invoke() }
            }
        } catch (t: Throwable) {
            throw NodeCreationException(klass, t)
        }

        public inline fun <reified T : Node> createRootNode(
            nodalConfig: NodalConfig,
            plugins: List<NodalPlugin> = emptyList(),
            noinline onRequestRemove: (Node) -> Unit,
            noinline dependencyDeclaration: DependencyDeclaration,
        ): T = createRootNode(
            T::class,
            nodalConfig,
            onRequestRemove,
            plugins,
            dependencyDeclaration
        ) as T

        public fun createRootNode(
            klass: KClass<out Node>,
            nodalConfig: NodalConfig = NodalConfig(),
            onRequestRemove: (Node) -> Unit,
            plugins: List<NodalPlugin> = emptyList(),
            dependencyDeclaration: DependencyDeclaration,
        ): Node {
            val mergedDependencyDeclaration: DependencyDeclaration = {
                val defaultPlugins = listOf(NodeTreeVisualiser())
                provides { NodalPlugins(defaultPlugins + plugins) }
                provides<NodalConfig> { nodalConfig }
                providesSelf<RemovalRequest> {
                    RemovalRequest(onRequestRemove)
                }

                include(dependencyDeclaration)
            }
            return instantiateNode(klass, createRootScope(), mergedDependencyDeclaration).also { node ->
                node.plugins.all.forEach { it.onEvent(NodeCreatedEvent(null, node)) }
            }
        }
    }
}