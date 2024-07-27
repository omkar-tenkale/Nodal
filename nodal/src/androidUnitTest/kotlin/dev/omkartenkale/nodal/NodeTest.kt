package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.Node.Companion.ui
import dev.omkartenkale.nodal.exceptions.ChildNodeNotFoundException
import dev.omkartenkale.nodal.exceptions.DisallowedNodeAdditionException
import dev.omkartenkale.nodal.exceptions.NodeCreationException
import dev.omkartenkale.nodal.util.MainDispatcherRule
import dev.omkartenkale.nodal.util.child
import dev.omkartenkale.nodal.util.doOnAdded
import dev.omkartenkale.nodal.util.doOnRemoved
import dev.omkartenkale.nodal.util.isAdded
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class NodeTest {
    class RootNode : Node()
    class ANode : Node()
    class BNode : Node()

    private lateinit var rootNode: RootNode

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Before
    fun setUp() {
        rootNode = Node.createRootNode(
            klass = RootNode::class,
            nodalConfig = NodalConfig(false),
            onRequestRemove = { }) {} as RootNode
    }

    @Test
    fun `verify node addition`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        assert(rootNode.children.contains(nodeA))
        assert(nodeA.isAdded)

        val nodeB = rootNode.addChild<BNode>()
        assert(rootNode.children.contains(nodeB))
        assert(nodeB.isAdded)
        assert(rootNode.children.size == 2)
    }

    @Test
    fun `verify node removal`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        nodeA.removeSelf()
        assert(nodeA.isAdded.not())
        assert(rootNode.children.contains(nodeA).not())
    }

    @Test
    fun `verify node scope cancelled after removed`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        nodeA.removeSelf()
        assert(nodeA.coroutineScope.isActive.not())
    }

    @Test
    fun `verify node added callback is invoked`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val nodeA = rootNode.addChild<ANode>()
        nodeA.doOnAdded {
            callback()
        }
        verify { callback.invoke() }
    }

    @Test
    fun `verify node isDead property is false before removed`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        assert(nodeA.isDead.not())
    }
    @Test
    fun `verify child not found`() = runTest {
        assertFailsWith<ChildNodeNotFoundException> {
            rootNode.child<BNode>()
        }
    }

    @Test
    fun `verify node isDead property is updated after removed`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        nodeA.removeSelf()
        assert(nodeA.isDead)
    }

    @Test
    fun `verify node removed callback is invoked`() = runTest {
        val callback = mockk<() -> Unit>(relaxed = true)
        val nodeA = rootNode.addChild<ANode>()
        nodeA.doOnRemoved {
            callback()
        }
        nodeA.removeSelf()
        verify { callback.invoke() }
    }

    @Test
    fun `verify node doOnInit callback is invoked`() = runTest {
        class SomeNode : Node() {
            var doOnInitCalled = false

            init {
                doOnInit {
                    doOnInitCalled = true
                }
            }
        }
        rootNode.addChild<SomeNode>().apply {
            assert(doOnInitCalled)
        }
    }

    @Test
    fun `verify node does not have ui dependency in unit tests`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        assertFails {
            nodeA.ui
        }
    }


    @Test
    fun `verify child cannot be added on a dead node`() = runTest {
        val nodeA = rootNode.addChild<ANode>()
        nodeA.removeSelf()
        assertFailsWith(DisallowedNodeAdditionException::class){
            nodeA.addChild<ANode>()
        }
    }

    @Test
    fun `verify error is thrown when node could not be instantiated`() = runTest {
        class SomeNode(unit: Unit): Node()

        assertFailsWith(NodeCreationException::class){
            rootNode.addChild<SomeNode>()
        }
    }

}