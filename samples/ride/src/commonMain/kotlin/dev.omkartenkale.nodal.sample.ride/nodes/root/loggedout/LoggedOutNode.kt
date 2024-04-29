@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.nodes.root.LoggedInCallback
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.signup

class OTPConfirmedCallback(block: (String) -> Unit) : Callback<String> by Callback(block)

fun Node.addLoggedOutNode(onLoggedIn: (String) -> Unit) =
    addChild<LoggedOutNode> {
        provides<LoggedInCallback> { LoggedInCallback { onLoggedIn(it) } }
    }

class LoggedOutNode : Node() {

    val onLoggedIn: LoggedInCallback by dependencies()

    override fun onAdded() {
        draw {
            Image(
                modifier = Modifier.fillMaxSize().clickable {
                    addChild<ConfirmOtpNode> {
                        provides<OTPConfirmedCallback> {
                            OTPConfirmedCallback {
                                onLoggedIn("Radical")
                                removeSelf()
                            }
                        }
                    }
                },
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                painter = painterResource(Res.drawable.signup),
                contentDescription = null
            )
        }
    }
}