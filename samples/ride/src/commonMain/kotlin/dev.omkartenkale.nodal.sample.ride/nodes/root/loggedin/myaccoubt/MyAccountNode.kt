package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.myaccoubt

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.transitions.BackstackTransition
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.misc.BackPressCallback
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.RideNode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.home
import nodal.ride.generated.resources.myaccount
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class MyAccountNode: Node() {

    private lateinit var layer: UI.Layer

    @OptIn(ExperimentalResourceApi::class)
    override fun onAdded() {
        backPressHandler.addBackPressCallback(BackPressCallback(isEnabled = true) {
            removeSelf()
        })
        layer = ui.draw(TransitionSpec.Slide) {
            Column {

                Image(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().clickable {
//                        addChild<MyAccountNode>()
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.myaccount),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }
    }

    override fun onRemoved() {
        super.onRemoved()
        layer.destroy()
    }
}
