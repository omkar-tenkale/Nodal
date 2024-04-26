package dev.omkartenkale.nodal.exceptions

import kotlin.reflect.KClass

public class DependencyNotFoundException(dependency: KClass<*>, scopeName: String, t: Throwable) : NodalException("Dependency ${dependency.simpleName} (${dependency.qualifiedName}) couldn't be resolved by $scopeName",t)