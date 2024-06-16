@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.paymentmode

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.util.addChild
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.add_payment
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

fun Node.addSelectedPaymentModeNode(onPaymentModeChanged: (String) -> Unit) =
    addChild<SelectedPaymentModeNode>(PaymentModeChangedCallback { onPaymentModeChanged(it) })

class PaymentModeChangedCallback(block: (String)-> Unit): Callback<String> by Callback(block)
class SelectedPaymentModeNode : Node() {

    private val onPaymentModeChanged by dependencies<PaymentModeChangedCallback>()

    @Composable
    fun Content() {
        Box(Modifier.fillMaxWidth()) {
            var selectedPaymentMode by remember { mutableStateOf<String?>(null) }
            selectedPaymentMode?.let {
                Text(
                    text = "Paying with $it", color = Color.White, /*fontSize = 32.dp, color = Color.White*/
                    modifier = Modifier.background(Color.Black).padding(16.dp).fillMaxWidth().clickable {
                        addPaymentSelectionSelectionNode {
                            selectedPaymentMode = it
                            onPaymentModeChanged(it)
                        }
                    })
            } ?: kotlin.run {
                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        addPaymentSelectionSelectionNode {
                            selectedPaymentMode = it
                            onPaymentModeChanged(it)
                        }
                    },
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(Res.drawable.add_payment),
                    contentDescription = null
                )
            }
        }
    }
}