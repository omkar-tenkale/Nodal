package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.route

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import dev.omkartenkale.nodal.util.addChild
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.address_selection
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

fun Node.selectAddress(onAddressSelected: (String) -> Unit) =
    addChild<AddressSelectionNode>(AddressSelectionCallback { onAddressSelected(it) })

class AddressSelectionCallback(block: (String)-> Unit): Callback<String> by Callback(block)
@OptIn(ExperimentalResourceApi::class)
class AddressSelectionNode : Node() {

    private val onAddressSelected by dependencies<AddressSelectionCallback>()

    private lateinit var layer: UI.Layer

    override fun onAdded() {

        layer = ui.draw {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = nonExpandingSheetState(),
            ) {
                Image(
                    modifier = Modifier.clickable {
                        onAddressSelected.invoke("Airport Road")
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.address_selection),
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