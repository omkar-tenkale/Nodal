@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.completed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.misc.VoidCallback
import dev.omkartenkale.nodal.util.addChild
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.rate_driver
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

class DriverRatedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

fun Node.addDriverRatingNode(onDriverRated: () -> Unit) =
    addChild<DriverRatingNode>(DriverRatedCallback(onDriverRated))

class DriverRatingNode : Node() {
    val driverRatedCallback: DriverRatedCallback by dependencies()

    override fun onAdded() {
        draw(TransitionSpec.Slide) {
            var submitVisible by remember { mutableStateOf(false) }
            Column {
                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        submitVisible = true
                    },
                    painter = painterResource(Res.drawable.rate_driver),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
                if (submitVisible) {
                    Text(text = "RATE", modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp).clickable {
                        driverRatedCallback()
                        removeSelf()
                    }, color = Color.White)
                }
            }
        }
    }
}