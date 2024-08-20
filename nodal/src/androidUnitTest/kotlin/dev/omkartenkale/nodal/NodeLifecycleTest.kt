package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.lifecycle.ChildrenUpdatedEvent
import dev.omkartenkale.nodal.misc.VoidCallback
import dev.omkartenkale.nodal.util.MainDispatcherRule
import dev.omkartenkale.nodal.util.child
import dev.omkartenkale.nodal.util.doOnAdded
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class NodeLifecycleTest {
    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun `verify dispatch added triggers onAdded callback`() = runTest {
        class OnAddedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

        class RootNode : Node(){
            override fun onAdded() {
                dependencies.get<OnAddedCallback>()()
            }
        }

        var onAddedCalled = false
        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = {}) {
            provides {
                OnAddedCallback {
                    onAddedCalled = true
                }
            }
        } as RootNode

        rootNode.dispatchAdded()
        assertTrue(onAddedCalled)
    }

    @Test
    fun `verify adding children triggers onAdded callback`() = runTest {
        class OnAddedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

        class RootNode : Node()
        class NodeA : Node() {
            override fun onAdded() {
                dependencies.get<OnAddedCallback>()()
            }
        }

        var onAddedCalled = false
        val rootNode =
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        rootNode.addChild<NodeA> {
            provides {
                OnAddedCallback {
                    onAddedCalled = true
                }
            }
        }
        assertTrue(onAddedCalled)
    }

    @Test
    fun `verify dispatch removed triggers onRemoved callback`() = runTest {
        class OnRemovedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

        class RootNode : Node(){
            override fun onRemoved() {
                dependencies.get<OnRemovedCallback>()()
            }
        }

        var onRemovedCalled = false
        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = {}) {
            provides {
                OnRemovedCallback {
                    onRemovedCalled = true
                }
            }
        } as RootNode

        rootNode.dispatchRemoved()
        assertTrue(onRemovedCalled)
    }

    @Test
    fun `verify removing children triggers onAdded callback`() = runTest {
        class OnRemovedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

        class RootNode : Node(){
            fun removeNodeA() = removeChild(children.first())
        }
        class NodeA : Node() {
            override fun onRemoved() {
                dependencies.get<OnRemovedCallback>()()
            }
        }

        var onRemovedCalled = false
        val rootNode =
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        rootNode.addChild<NodeA> {
            provides {
                OnRemovedCallback {
                    onRemovedCalled = true
                }
            }
        }
        rootNode.removeNodeA()
        assertTrue(onRemovedCalled)
    }

    @Test
    fun `verify child added event is fired`() = runTest {
        class RootNode : Node()
        class NodeA : Node()

        val rootNode =
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        var callbackFired = false
        rootNode.childrenUpdatedEvents.filter { it is ChildrenUpdatedEvent.NodeAdded && it.node is NodeA }
            .onEach {
                callbackFired = true
            }.launchIn(rootNode.coroutineScope)

        rootNode.addChild<NodeA>()
        assertTrue(callbackFired)
    }

    @Test
    fun `verify child removed event is fired`() = runTest {
        class NodeA : Node()

        class RootNode : Node() {
            fun removeNodeA() {
                removeChild(child<NodeA>())
            }
        }

        val rootNode =
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        var callbackFired = false
        rootNode.childrenUpdatedEvents.filter { it is ChildrenUpdatedEvent.NodeRemoved && it.node is NodeA }
            .onEach {
                callbackFired = true
            }.launchIn(rootNode.coroutineScope)

        rootNode.addChild<NodeA>()
        rootNode.removeNodeA()

        assertTrue(callbackFired)
    }
}