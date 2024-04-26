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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RootNode: Node() {

    override fun onAdded() {

        dependencies.get<UI>().draw {
            Content()
        }

        coroutineScope.launch {
            delay(2000)
            addChild<LoginNode>()
        }
    }

    @Composable
    fun Content(){
        Box(modifier = Modifier.width(100.dp).height(100.dp).background(Color.Black))
    }
}