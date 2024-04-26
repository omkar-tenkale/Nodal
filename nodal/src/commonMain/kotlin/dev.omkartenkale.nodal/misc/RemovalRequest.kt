package dev.omkartenkale.nodal.misc

public class RemovalRequest(private val onRequestRemove: () -> Unit) {
    public operator fun invoke(): Unit = onRequestRemove()
}