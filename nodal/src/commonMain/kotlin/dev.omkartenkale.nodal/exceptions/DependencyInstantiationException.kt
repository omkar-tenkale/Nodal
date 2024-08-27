package dev.omkartenkale.nodal.exceptions

public class DependencyInstantiationException(t: Throwable) : NodalException("Failed to instantiate node's dependency", t)