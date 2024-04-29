package dev.omkartenkale.nodal.lifecycle

import dev.omkartenkale.nodal.Node

public sealed class ChildrenUpdatedEvent{
    public class NodeAdded(public val node: Node): ChildrenUpdatedEvent()
    public class NodeRemoved(public val node: Node): ChildrenUpdatedEvent()
}