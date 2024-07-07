package dev.omkartenkale.nodal.misc

public interface BackPressHandler {
    public fun dispatchBackPress()
    public fun addBackPressCallback(backPressCallback: BackPressCallback)
}

public class BackPressCallback(public var isEnabled: Boolean, public val block: () -> Unit) {
    public operator fun invoke(): Unit = block()
}