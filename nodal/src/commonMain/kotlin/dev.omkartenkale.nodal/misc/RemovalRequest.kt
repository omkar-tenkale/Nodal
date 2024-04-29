package dev.omkartenkale.nodal.misc

import dev.omkartenkale.nodal.Node

public class RemovalRequest(private val onRequestRemove: (Node) -> Unit) {
    public operator fun invoke(node: Node): Unit = onRequestRemove(node)
}