package dev.omkartenkale.nodal.plugin

import dev.omkartenkale.nodal.Node

public class NodalPlugins(public val all: List<NodalPlugin>)

public interface NodalPlugin {
    public interface Event

    public fun onEvent(event: Event)
}

public class NodeCreatedEvent(public val parent: Node?, public val node: Node) : NodalPlugin.Event
public class NodeAddedEvent(public val parent: Node?, public val node: Node) : NodalPlugin.Event
public class NodeRemovedEvent(public val parent: Node?, public val node: Node) : NodalPlugin.Event