@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.paymentmode

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import dev.omkartenkale.nodal.util.addChild
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.driver_arriving
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.payment_methods

fun Node.addPaymentSelectionSelectionNode(onPaymentModeSelected: (String) -> Unit) =
    addChild<PaymentModeSelectionNode>(PaymentModeSelectionCallback { onPaymentModeSelected(it) })

class PaymentModeSelectionCallback(block: (String)-> Unit): Callback<String> by Callback(block)
class PaymentModeSelectionNode : Node() {

    private val onPaymentModeSelected by dependencies<PaymentModeSelectionCallback>()

    override fun onAdded() {
        ui.draw {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = nonExpandingSheetState(),
            ) {
                Image(
                    modifier = Modifier.clickable {
                        onPaymentModeSelected.invoke("CASH")
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.payment_methods),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }
}