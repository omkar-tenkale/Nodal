package dev.omkartenkale.nodal.util

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.plugin.NodeAddedEvent
import dev.omkartenkale.nodal.plugin.NodeRemovedEvent

public object RootNodeUtil {
    public fun dispatchAdded(node: Node){
        node.dispatchAdded()
        node.plugins.all.forEach { it.onEvent(NodeAddedEvent(null, node)) }
    }
    public fun dispatchRemoved(node: Node){
        node.dispatchRemoved()
        node.plugins.all.forEach { it.onEvent(NodeRemovedEvent(null, node)) }
    }
}