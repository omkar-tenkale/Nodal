package dev.omkartenkale.nodal.misc

public interface Callback<T> {
    public operator fun invoke(result: T)
}

public fun <T> Callback(block: (T)-> Unit): Callback<T> = object : Callback<T>{
    override fun invoke(result: T) {
        block(result)
    }
}


public interface VoidCallback {
    public operator fun invoke()
}

public fun VoidCallback(block: ()-> Unit): VoidCallback = object : VoidCallback{
    override fun invoke() {
        block()
    }
}