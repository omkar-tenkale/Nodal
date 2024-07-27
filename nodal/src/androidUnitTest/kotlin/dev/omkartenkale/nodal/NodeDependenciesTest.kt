package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.plugin.NodalPlugins
import dev.omkartenkale.nodal.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.fail

class NodeDependenciesTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Before
    fun setUp() {

    }

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


}