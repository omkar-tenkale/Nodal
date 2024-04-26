package dev.omkartenkale.nodal.misc

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.Scope
import kotlin.reflect.KClass

public expect fun <T : Node> KClass<T>.instantiate(): T
