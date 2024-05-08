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
import dev.omkartenkale.nodal.Node.Companion.ui
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.addLoggedInNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedout.addLoggedOutNode
import dev.omkartenkale.nodal.util.doOnRemoved
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoggedInCallback(block: (String)-> Unit): Callback<String> by Callback(block)

class RootNode : Node() {
    override fun onAdded() {
        childrenUpdatedEvents.onEach {
            if(children.isEmpty()){
                removeSelf()
            }
        }.launchIn(coroutineScope)
        addLoggedOutNode { userName ->
            addLoggedInNode(userName)
        }
    }
}