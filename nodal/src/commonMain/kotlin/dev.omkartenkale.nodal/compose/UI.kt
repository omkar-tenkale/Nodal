package dev.omkartenkale.nodal.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.Node.Companion.ui
import dev.omkartenkale.nodal.compose.transitions.Backstack
import dev.omkartenkale.nodal.compose.transitions.BackstackTransition
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.util.doOnRemoved
import kotlinx.coroutines.flow.MutableStateFlow

public class UI {
    private var layers by mutableStateOf<List<Layer>>(emptyList())
    public val focusState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    @Composable
    public fun Content() {
        Backstack(backstack = layers)
    }

    public fun draw(transitionSpec: TransitionSpec = TransitionSpec.None, content: @Composable (Modifier) -> Unit): Layer {
        return Layer(transitionSpec = transitionSpec, content = content) {
            layers -= it
        }.also {
            layers += it
        }
    }

    public suspend fun dispatchFocusChanged(isFocused: Boolean) {
        focusState.emit(isFocused)
    }

    public class Layer(public val transitionSpec: TransitionSpec, public val content: @Composable (Modifier) -> Unit, internal val onDestroy: (Layer)->Unit) {

        @Composable
        public fun Content() {
            content(Modifier.fillMaxSize())
        }

        public fun destroy() {
            onDestroy(this)
        }

    }
}

private fun List<UI.Layer>.secondToTop(): UI.Layer? = if(size < 2 ) null else get(lastIndex-1)

public fun Node.draw(content: @Composable (Modifier) -> Unit) {
    val layer = ui.draw(content = content)
    doOnRemoved {
        layer.destroy()
    }
}