package dev.omkartenkale.nodal.exceptions

import kotlin.reflect.KClass

public class DisallowedNodeAdditionException(parent: KClass<*>, child: KClass<*>)
    : IllegalNodeOperationException("Cannot add a child to a dead node\nParent ${parent.simpleName} (${parent.qualifiedName})\nChild ${child.simpleName} (${child.qualifiedName})")