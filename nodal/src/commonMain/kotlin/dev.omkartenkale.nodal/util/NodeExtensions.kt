package dev.omkartenkale.nodal.util

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.exceptions.ChildNodeNotFoundException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

public val Node.isAdded: Boolean get() = isAddedEvents.value

public fun Node.doOnAdded(block: () -> Unit) {
    isAddedEvents.filter { it }.onEach { block() }.launchIn(coroutineScope)
}

public fun Node.doOnRemoved(block: () -> Unit) {
    isAddedEvents.filter { it.not() }.onEach {
        block()
    }.launchIn(coroutineScope)
}

public inline fun <reified T : Node> Node.child(): T =
    childOrNull<T>() ?: throw ChildNodeNotFoundException(T::class)

public inline fun <reified T : Node> Node.childOrNull(): T? = children.firstOrNull { it is T } as? T

public inline fun <reified T : Node> Node.addChild(vararg params: Any): T = addChild(T::class, *params) as T
public fun Node.addChild(node: KClass<out Node>, vararg params: Any): Node {
    return addChild(node){
        params.forEach {
            provides(it::class) { it }
        }
    }
}