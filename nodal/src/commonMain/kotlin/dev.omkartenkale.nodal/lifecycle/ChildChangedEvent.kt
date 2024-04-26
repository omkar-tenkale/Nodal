package dev.omkartenkale.nodal.lifecycle

import dev.omkartenkale.nodal.Node

public sealed class ChildChangedEvent{
    public class NodeAdded(public val node: Node): ChildChangedEvent()
    public class NodeRemoved(public val node: Node): ChildChangedEvent()
}