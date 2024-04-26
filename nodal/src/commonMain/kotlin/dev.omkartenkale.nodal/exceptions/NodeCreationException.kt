package dev.omkartenkale.nodal.exceptions

import kotlin.reflect.KClass

public class NodeCreationException(private val node: KClass<*>, t: Throwable) : NodalException("${node.simpleName} failed to instantiate", t)