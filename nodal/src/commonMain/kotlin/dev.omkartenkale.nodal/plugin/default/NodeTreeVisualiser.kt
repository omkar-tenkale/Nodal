package dev.omkartenkale.nodal.plugin.default

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.plugin.NodalPlugin
import dev.omkartenkale.nodal.plugin.NodeAddedEvent
import dev.omkartenkale.nodal.plugin.NodeRemovedEvent

internal class NodeTreeVisualiser: NodalPlugin {
    private var rootNode: Node? = null

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

    private fun Node.printHierarchy(depth: Int = 0, printQualifiedName: Boolean = false) {
        val prefix = "    ".repeat(depth)
        val name = if (printQualifiedName) this::class.qualifiedName else this::class.simpleName
        println(
            "$prefix├── $name"
        )

        for (child in children.dropLast(1)) {
            child.printHierarchy(depth + 1, printQualifiedName)
        }

        if (children.isNotEmpty()) {
            children.last().printHierarchy(depth + 1, printQualifiedName)
        }
    }
}