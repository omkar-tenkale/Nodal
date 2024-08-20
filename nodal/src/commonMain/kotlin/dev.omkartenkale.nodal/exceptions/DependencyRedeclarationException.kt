package dev.omkartenkale.nodal.exceptions

public class DependencyRedeclarationException(dependency: String, node: String) : IllegalNodeOperationException("Not allowed to re-declare dependency $dependency\n in node $node)")