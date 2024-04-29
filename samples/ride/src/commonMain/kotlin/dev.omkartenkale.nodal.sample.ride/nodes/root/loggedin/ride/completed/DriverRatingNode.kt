@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.completed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.misc.VoidCallback
import dev.omkartenkale.nodal.util.addChild
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.finding_driver

class DriverRatedCallback(block: () -> Unit) : VoidCallback by VoidCallback(block)

fun Node.addDriverRatingNode(onDriverRated: () -> Unit) =
    addChild<DriverRatingNode>(DriverRatedCallback(onDriverRated))

class DriverRatingNode : Node() {
    val driverRatedCallback: DriverRatedCallback by dependencies()
    override fun onAdded() {
        ui.draw {
            var submitVisible by remember { mutableStateOf(false) }
            Column {
                Image(
                    modifier = Modifier.clickable {
                        submitVisible = true
                    },
                    painter = painterResource(Res.drawable.finding_driver),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                if (submitVisible) {
                    Text(text = "RATE", modifier = Modifier.background(Color.Black).clickable {
                        driverRatedCallback()
                        removeSelf()
                    }, color = Color.White)
                }
            }

        }
    }
}