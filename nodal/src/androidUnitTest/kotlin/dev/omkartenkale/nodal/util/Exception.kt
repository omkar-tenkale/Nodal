package dev.omkartenkale.nodal.util

import dev.omkartenkale.nodal.Node
import kotlin.test.fail

inline fun <reified T> assertNestedException(block: () -> Unit) {
    runCatching {
        block()
    }.onFailure {
        var exceptionFound = false
        var cause: Throwable? = it
        while (cause != null){
            if(cause::class ==  T::class) {
                exceptionFound = true
            }
            cause = cause.cause
        }
        if(exceptionFound.not()){
            fail("Exception ${T::class.simpleName} was not thrown")
        }
    }.onSuccess {
        fail("Exception ${T::class.simpleName} was not thrown")
    }
}


fun assertDoesNotThrowException(block: () -> Unit) {
    runCatching {
        block()
    }.onFailure {
        it.printStackTrace()
       fail("Exception was not expected but was thrown ${it::class.simpleName} ")
    }
}

