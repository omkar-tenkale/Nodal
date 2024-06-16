@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.nodes.root.LoggedInCallback
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.signup
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
class OTPConfirmedCallback(block: (String) -> Unit) : Callback<String> by Callback(block)

fun Node.addLoggedOutNode(onLoggedIn: (String) -> Unit) =
    addChild<LoggedOutNode> {
        provides<LoggedInCallback> { LoggedInCallback { onLoggedIn(it) } }
    }

class LoggedOutNode : Node() {

    val onLoggedIn: LoggedInCallback by dependencies()
    private lateinit var layer: UI.Layer

    override fun onAdded() {
        layer = ui.draw {
            Box {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.TopCenter,
                    painter = painterResource(Res.drawable.signup),
                    contentDescription = null
                )
                Box(modifier = Modifier.padding(top = 100.dp).fillMaxWidth().height(50.dp).clickable {
                    addChild<ConfirmOtpNode> {
                        provides<OTPConfirmedCallback> {
                            OTPConfirmedCallback {
                                onLoggedIn("Radical")
                                removeSelf()
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onRemoved() {
        layer.destroy()
    }
}