package dev.omkartenkale.nodal.misc

import dev.omkartenkale.nodal.Node
import kotlin.reflect.KClass

public actual fun <T : Node> KClass<T>.instantiate(): T {
    return java.newInstance()
}