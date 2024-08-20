package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.plugin.default.NodeTreeVisualiser
import dev.omkartenkale.nodal.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NodeTreeVisualiserTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    @Test
    fun `verify node visualization is correct`() = runTest {
        class RootNode: Node()
        class NodeA: Node()
        class NodeB: Node()
        class NodeC: Node()
        class NodeD: Node()
        class NodeE: Node()

        val rootNode = Node.createRootNode(klass = RootNode::class, onRequestRemove = { }) {} as RootNode

        rootNode.apply {
            addChild<NodeA>().addChild<NodeC>()
            addChild<NodeB>().apply {
                addChild<NodeD>()
                addChild<NodeE>()
            }
        }

        assertEquals(
            """
                └── RootNode
                    ├── NodeA
                    │   └── NodeC
                    └── NodeB
                        ├── NodeD
                        └── NodeE

            """.trimIndent(),
            NodeTreeVisualiser().generateSubTreeRepresentation(rootNode))
    }
}