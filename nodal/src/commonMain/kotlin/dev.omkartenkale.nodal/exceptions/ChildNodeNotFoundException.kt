package dev.omkartenkale.nodal.exceptions

import kotlin.reflect.KClass

public class ChildNodeNotFoundException(node: KClass<*>) : NodalException("Child node ${node.simpleName} (${node.qualifiedName}) couldn't be found")