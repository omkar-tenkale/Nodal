package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.exceptions.DisallowedNodeAdditionException
import dev.omkartenkale.nodal.exceptions.NodeCreationException
import dev.omkartenkale.nodal.lifecycle.ChildChangedEvent
import dev.omkartenkale.nodal.misc.RemovalRequest
import dev.omkartenkale.nodal.misc.instantiate
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

internal val nodeId = atomic(0)

public open class Node {

    private lateinit var _scope: Scope
    public val dependencies: Scope
        get() = if (::_scope.isInitialized) {
            _scope
        } else error("Accessing dependencies in init block is not supported")

    public companion object {
        private fun instantiateNode(
            klass: KClass<out Node>,
            parentScope: Scope,
            dependencyDeclaration: DependencyDeclaration
        ): Node = try {
            val newNodeId = nodeId.getAndIncrement()
            val nodeScope = Scope("${klass.simpleName}#$newNodeId", parentScope)
            klass.instantiate().also {
                it.init(nodeScope)
                DependencyDeclarationDSL(nodeScope).apply(dependencyDeclaration)
                    .apply(it.providesDependencies)
            }
        } catch (t: Throwable) {
            throw NodeCreationException(klass, t)
        }

        public inline fun <reified T : Node> createRootNode(
            noinline dependencyDeclaration: DependencyDeclaration
        ): T = createRootNode(T::class, dependencyDeclaration) as T

        public fun createRootNode(
            klass: KClass<out Node>, dependencyDeclaration: DependencyDeclaration
        ): Node = instantiateNode(klass, createRootScope(), dependencyDeclaration)
    }

    private fun init(scope: Scope) {
        _scope = scope
    }

    private val removalRequest: RemovalRequest by dependencies<RemovalRequest>()

    public open val providesDependencies: DependencyDeclaration = {}
    public val stateChangedEvents: MutableStateFlow<NodeLifecycleState> =
        MutableStateFlow(NodeLifecycleState.INITIALIZED)
    public val childChangedEvents: MutableSharedFlow<ChildChangedEvent> = MutableSharedFlow()

    protected var isAdded: Boolean = false
        set(value) {
            field = value
            stateChangedEvents.value =
                if (value) NodeLifecycleState.ADDED else NodeLifecycleState.REMOVED
        }

    protected inline fun <reified T : Any> dependencies(): Lazy<T> = lazy {
        dependencies.get<T>()
    }
//        .also {
//            if (dependencies.get<NodalConfig>().createEagerInstances) {
//                it.value
//            }
//        }

    private val _children = mutableListOf<Node>()
    public val children: List<Node> = _children

    protected var coroutineScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    public inline fun <reified T : Node> addChild(noinline dependencyDeclaration: DependencyDeclaration = {}): T =
        addChild(T::class, dependencyDeclaration) as T

    public fun addChild(
        klass: KClass<out Node>, dependencyDeclaration: DependencyDeclaration
    ): Node {
        if (stateChangedEvents.value == NodeLifecycleState.REMOVED) {
            throw DisallowedNodeAdditionException(this::class, klass)
        }
        val dependencyDeclaration: DependencyDeclaration = {
            providesSelf<RemovalRequest> {
                RemovalRequest {
                    removeChild(children.first())
                }
            }
            include(dependencyDeclaration)
        }
        val node = instantiateNode(klass, dependencies, dependencyDeclaration)
        _children.add(node)
        node.dispatchAdded()
        coroutineScope.launch { childChangedEvents.emit(ChildChangedEvent.NodeAdded(node)) }
        return node
    }

    public fun removeChild(node: Node) {
        if (children.contains(node)) {
            node.dispatchRemoved()
            _children.remove(node)
            coroutineScope.launch { childChangedEvents.emit(ChildChangedEvent.NodeRemoved(node)) }
        } else error("Cant remove node as parent does not contain child. \n\tParent: ${this::class.simpleName} (${this::class.qualifiedName}), \n\tChild: ${node::class.simpleName} (${node::class.qualifiedName})")
    }

    public open fun handleDetachRequest(node: Node): Boolean {
        removeChild(node)
        return true
    }

    public fun dispatchAdded() {
        onAdded()
        isAdded = true
    }

    public open fun onAdded(): Unit = Unit

    internal fun dispatchRemoved() {
        children.forEach { it.dispatchRemoved() }
        isAdded = false
        onRemoved()
        coroutineScope.cancel()
        dependencies.close()
    }

    public open fun onRemoved(): Unit = Unit
    protected fun removeSelf(): Unit = removalRequest.invoke()

    public fun Node.onAdded(block: () -> Unit) {
        stateChangedEvents.filter { it == NodeLifecycleState.ADDED }.onEach { block() }
            .launchIn(coroutineScope)
    }

    public fun Node.onRemoved(block: () -> Unit) {
        stateChangedEvents.filter { it == NodeLifecycleState.REMOVED }.onEach { block() }
            .launchIn(coroutineScope)
    }
}