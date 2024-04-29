package dev.omkartenkale.nodal.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.Node.Companion.ui
import dev.omkartenkale.nodal.util.doOnRemoved
import kotlinx.coroutines.flow.MutableStateFlow

public class UI {
    private val layers = mutableStateListOf<Layer>()

    public val focusState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @Composable
    public fun drawLayers() {
        remember { layers }.forEach {
            it.draw()
        }
    }

    public fun draw(content: @Composable (Modifier) -> Unit): Layer {
        return Layer(content) {
            layers.remove(it)
        }.also {
            layers.add(it)
        }
    }

    public suspend fun dispatchFocusChanged(isFocused: Boolean) {
        focusState.emit(isFocused)
    }

    public class Layer(public val content: @Composable (Modifier) -> Unit, internal val onDestroy: (Layer)->Unit) {

        @Composable
        public fun draw() {
            content(Modifier.fillMaxSize())
        }

        public fun destroy() {
            onDestroy(this)
        }
    }
}

public fun Node.draw(content: @Composable (Modifier) -> Unit) {
    val layer = ui.draw(content)
    doOnRemoved { layer.destroy() }
}