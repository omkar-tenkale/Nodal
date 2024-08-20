package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.exceptions.DependencyRedeclarationException
import dev.omkartenkale.nodal.util.MainDispatcherRule
import dev.omkartenkale.nodal.util.assertDoesNotThrowException
import dev.omkartenkale.nodal.util.assertNestedException
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class DependencyAccessibilityTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun `verify provided dependency accessible in self`() {
        class Some

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        assertNotNull(rootNode.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify provided dependency at initialization is accessible in self`() {
        class Some
        class RootNode : Node()

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {
            provides { Some() }
        } as RootNode
        assertNotNull(rootNode.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify self provided dependency at initialization is accessible in self`() {
        class Some
        class RootNode : Node()

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {
            providesSelf { Some() }
        } as RootNode
        assertNotNull(rootNode.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify dependency provided with type is accessible in self`() {
        open class Some
        class Some2: Some()

        class NodeA: Node()

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides<Some> { Some2() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        val deps = rootNode.addChild<NodeA>().dependencies
        assertNull(deps.getOrNull<Some2>())
        assertNotNull(deps.getOrNull<Some>())
        assert(deps.get<Some>()::class == Some2::class)
    }

    @Test
    fun `verify provided dependency with qualifier is accessible in self`() {
        class Some(val value: Int)
        class NodeA: Node()

        class SomeTypeQualifier

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides(qualifiedAs("some")) { Some(111) }
                provides(qualifiedAs<SomeTypeQualifier>()) { Some(222) }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode

        val deps = rootNode.addChild<NodeA>().dependencies
        assertNull(deps.getOrNull<Some>())
        assertEquals(deps.get<Some>(qualifiedAs("some")).value,111)
        assertEquals(deps.get<Some>(qualifiedAs<SomeTypeQualifier>()).value, 222)
    }

    @Test
    fun `verify provided dependency accessible in child`() {
        class Some
        class NodeA: Node()

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        val some = rootNode.addChild<NodeA>().dependencies.getOrNull<Some>()
        assert(some != null)
    }


    @Test
    fun `verify self declared dependency not accessible to child`() {
        class Some
        class NodeA: Node()

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                providesSelf { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        assertNull(rootNode.addChild<NodeA>().dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify provided dependency is accessible to grand child`() = runTest {
        class Some
        class NodeA: Node()
        class NodeB: Node()

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode

        val nodeA = rootNode.addChild<NodeA>()
        val nodeB = nodeA.addChild<NodeB>()

        assertNotNull(nodeB.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify child provided dependency not accessible in parent`() {
        class Some
        class RootNode : Node()

        class NodeA : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        rootNode.addChild<NodeA>()
        assertNull(rootNode.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify child provided self dependency not accessible to parent`() {
        class Some
        class RootNode : Node()

        class NodeA : Node() {
            override val providesDependencies: DependencyDeclaration = {
                providesSelf { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        rootNode.addChild<NodeA>()
        assertNull(rootNode.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify child provided dependency not accessible in sibling`() {
        class Some
        class RootNode : Node()
        class NodeA : Node()

        class NodeB : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        val nodeA = rootNode.addChild<NodeA>()
        val nodeB = rootNode.addChild<NodeB>()
        assertNull(nodeA.dependencies.getOrNull<Some>())
        assertNotNull(nodeB.dependencies.getOrNull<Some>())
    }


    @Test
    fun `verify dependency redeclaration in same node throws error`() {
        class Some
        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
                provides { Some() }
            }
        }
        assertNestedException<DependencyRedeclarationException>{
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        }
    }

    @Test
    fun `verify dependency re-provided as self takes preference`() {
        class Some(val value: Int)
        class RootNode : Node() {
            val some by dependencies<Some>()
            override val providesDependencies: DependencyDeclaration = {
                provides { Some(111) }
                providesSelf { Some(222) }
            }
        }
        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        assertEquals(rootNode.some.value, 222)
        assertNotEquals(rootNode.some.value, 111)
    }

    @Test
    fun `verify dependency provided as non self does not take preference`() {
        class Some(val value: Int)
        class NodeA: Node()

        class RootNode : Node() {
            val some by dependencies<Some>()
            override val providesDependencies: DependencyDeclaration = {
                provides { Some(111) }
                providesSelf { Some(222) }
            }
        }
        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        val nodeA = rootNode.addChild<NodeA>()

        assertEquals(rootNode.some.value, 222)
        assertNotEquals(rootNode.some.value, 111)

        assertEquals(nodeA.dependencies.get<Some>().value, 111)
    }

    @Test
    fun `verify dependency redeclaration on initialization in same node throws error`() {
        class Some
        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }
        assertNestedException<DependencyRedeclarationException>{
            Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {
                provides { Some() }
            } as RootNode
        }
    }

    @Test
    fun `verify dependency declaration is local to each node`() {
        data class Some(val value: Int)

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some(111) }
            }
        }

        class NodeA: Node(){
            override val providesDependencies: DependencyDeclaration = {
                provides { Some(222) }
            }
        }

        class NodeB: Node(){
            override val providesDependencies: DependencyDeclaration = {
                providesSelf { Some(333) }
            }
        }

        class NodeC: Node()

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode
        val nodeA = rootNode.addChild<NodeA>()
        val nodeB = nodeA.addChild<NodeB>()
        val nodeC = nodeB.addChild<NodeC>()

        assertEquals(111, rootNode.dependencies.get<Some>().value)
        assertEquals(222, nodeA.dependencies.get<Some>().value)
        assertEquals(333,nodeB.dependencies.get<Some>().value)
        assertEquals( 222, nodeC.dependencies.get<Some>().value)
        assertNotEquals(333, nodeC.dependencies.get<Some>().value)
    }
}