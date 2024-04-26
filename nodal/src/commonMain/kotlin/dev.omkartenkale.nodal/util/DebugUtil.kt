package dev.omkartenkale.nodal.util

import dev.omkartenkale.nodal.Node

public fun Node.printHierarchy(depth: Int = 0, printQualifiedName: Boolean = false) {
    val prefix = "    ".repeat(depth)
    val name = if (printQualifiedName) this::class.qualifiedName else this::class.simpleName
    println(
        "$prefix├── $name"
    )

    for (child in children.dropLast(1)) {
        child.printHierarchy(depth + 1, printQualifiedName)
    }

    if (children.isNotEmpty()) {
        children.last().printHierarchy(depth + 1, printQualifiedName)
    }
}