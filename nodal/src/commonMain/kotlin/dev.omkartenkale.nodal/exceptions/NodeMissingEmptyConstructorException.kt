package dev.omkartenkale.nodal.exceptions

import kotlin.reflect.KClass

public class NodeMissingEmptyConstructorException(public val node: KClass<*>, e: Exception) : NodalException("${node.simpleName} must have an empty constructor", e)