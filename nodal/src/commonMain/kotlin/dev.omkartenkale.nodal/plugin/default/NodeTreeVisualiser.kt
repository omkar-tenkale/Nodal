package dev.omkartenkale.nodal.plugin.default

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.plugin.NodalPlugin
import dev.omkartenkale.nodal.plugin.NodeAddedEvent
import dev.omkartenkale.nodal.plugin.NodeRemovedEvent

internal class NodeTreeVisualiser: NodalPlugin {
    private var rootNode: Node? = null

    companion object {
        object Symbols {
            const val ARM_RIGHT = "└── "
            const val INTERSECTION = "├── "
            const val LINE = "│   "
            const val SPACE = "    "
            const val NEWLINE = "\n"
        }
    }

    override fun onEvent(event: NodalPlugin.Event) {
        when(event){
            is NodeAddedEvent -> {
                if(event.parent == null){
                    rootNode = event.node
                }
                rootNode?.printHierarchy()
            }
            is NodeRemovedEvent -> {
                event.parent?.let { rootNode?.printHierarchy() }
            }
        }
    }

    private fun Node.printHierarchy(){
        println(generateSubTreeRepresentation(this))
    }

    fun generateSubTreeRepresentation(node: Node, useQualifiedName: Boolean = false): String =
        StringBuilder().run {
            generateRouterSubtreeRecursive(node = node, useQualifiedName = useQualifiedName, stringBuilder = this)
            toString()
        }

    private fun generateRouterSubtreeRecursive(
        node: Node,
        prefix: String = "",
        isTail: Boolean = true,
        useQualifiedName: Boolean,
        stringBuilder: StringBuilder
    ) {
        val nodeName = if (useQualifiedName) node::class.qualifiedName else
            (node::class.simpleName ?: "Unknown")
        stringBuilder.append(prefix + (if (isTail) Symbols.ARM_RIGHT else Symbols.INTERSECTION) + nodeName + Symbols.NEWLINE)
        val children = node.children
        for (i in 0 until children.size - 1) {
            generateRouterSubtreeRecursive(
                node = children[i],
                prefix = prefix + if (isTail) Symbols.SPACE else Symbols.LINE,
                isTail = false,
                useQualifiedName = useQualifiedName,
                stringBuilder = stringBuilder,
            )
        }
        if (children.isNotEmpty()) {
            generateRouterSubtreeRecursive(
                node = children[children.size - 1],
                prefix = prefix + if (isTail) Symbols.SPACE else Symbols.LINE,
                isTail = true,
                useQualifiedName = useQualifiedName,
                stringBuilder = stringBuilder
            )
        }
    }
}