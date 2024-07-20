@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.paymentmode

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import dev.omkartenkale.nodal.util.addChild
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.payment_methods
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

fun Node.addPaymentSelectionSelectionNode(onPaymentModeSelected: (String) -> Unit) =
    addChild<PaymentModeSelectionNode>(PaymentModeSelectionCallback { onPaymentModeSelected(it) })

class PaymentModeSelectionCallback(block: (String)-> Unit): Callback<String> by Callback(block)
class PaymentModeSelectionNode : Node() {

    private val onPaymentModeSelected by dependencies<PaymentModeSelectionCallback>()

    override fun onAdded() {
        draw(TransitionSpec.None) {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = nonExpandingSheetState(),
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onPaymentModeSelected.invoke("CASH")
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.payment_methods),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }
    }
}