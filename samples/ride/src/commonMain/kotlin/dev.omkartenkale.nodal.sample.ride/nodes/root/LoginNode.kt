package dev.omkartenkale.nodal.sample.ride.nodes.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.util.child
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginNode: Node() {
    override fun onAdded() {
        draw {
            Content()
        }

        ui.focusState.onEach {
            println("In Focus: $it")
        }.launchIn(coroutineScope)

        coroutineScope.launch {
            delay(1000)
            removeSelf()
        }
    }
    @Composable
    fun Content(){
        Box(modifier = Modifier.width(200.dp).height(100.dp).background(Color.Red))
    }
}