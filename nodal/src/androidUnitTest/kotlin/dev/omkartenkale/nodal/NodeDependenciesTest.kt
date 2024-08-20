package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.exceptions.DependencyNotFoundException
import dev.omkartenkale.nodal.exceptions.DependencyRedeclarationException
import dev.omkartenkale.nodal.exceptions.NodeCreationException
import dev.omkartenkale.nodal.plugin.NodalPlugins
import dev.omkartenkale.nodal.util.MainDispatcherRule
import dev.omkartenkale.nodal.util.assertDoesNotThrowException
import dev.omkartenkale.nodal.util.assertNestedException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.fail

class NodeDependenciesTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun `verify node provides dependencies`() = runTest {
        class Some
        class ANode: Node()

        class RootNode : Node() {
            override val providesDependencies: DependencyDeclaration = {
                provides { Some() }
            }
        }

        val rootNode = Node.createRootNode(
            klass = RootNode::class,
            onRequestRemove = { }) {} as RootNode

        val some = rootNode.addChild<ANode>().dependencies.getOrNull<Some>()
        assert(some != null)
    }

    @Test
    fun `verify eager instances are created`() = runTest {
        class Some
        class CustomInstantiationException: Exception()

        class RootNode : Node() {
            val some: Some by dependencies<Some>()

            override val providesDependencies: DependencyDeclaration = {
                provides<Some> {
                    throw CustomInstantiationException()
                    Some()
                }
            }
        }

        assertFails {
            Node.createRootNode(
                klass = RootNode::class,
                nodalConfig = NodalConfig(createEagerInstances = true),
                onRequestRemove = { }) {} as RootNode
        }

        try {
            Node.createRootNode(
                klass = RootNode::class,
                nodalConfig = NodalConfig(createEagerInstances = false),
                onRequestRemove = { }) {} as RootNode

        }catch (e:Exception){
            var cause: Throwable? = e
            while (cause!= null){
                if(e is CustomInstantiationException) {
                    fail("Un-eager instance created eagerly")
                }
                cause = e.cause
            }
        }
    }

    @Test
    fun `verify providesSelf does not expose dependencies to children`(){
        class Some
        class NodeA: Node()

        class RootNode : Node() {
            val some: Some by dependencies<Some>()

            override val providesDependencies: DependencyDeclaration = {
                providesSelf<Some> {
                    Some()
                }
            }
        }

        val rootNode = Node.createRootNode(
            klass = RootNode::class,
            nodalConfig = NodalConfig(createEagerInstances = true),
            onRequestRemove = { }) {} as RootNode

        val nodeA = rootNode.addChild<NodeA>()
        assertEquals(null,nodeA.dependencies.getOrNull<Some>())
    }

    @Test
    fun `verify get dependency should throw error if not available`(){
        class Some
        class RootNode : Node()

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode

        assertFailsWith(DependencyNotFoundException::class){
            rootNode.dependencies.get<Some>()
        }
    }


}