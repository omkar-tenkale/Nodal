package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.route

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
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.address_selection
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.route_selection


fun Node.selectAddress(onAddressSelected: (String) -> Unit) =
    addChild<AddressSelectionNode>(AddressSelectionCallback { onAddressSelected(it) })

class AddressSelectionCallback(block: (String)-> Unit): Callback<String> by Callback(block)
@OptIn(ExperimentalResourceApi::class)
class AddressSelectionNode : Node() {

    private val onAddressSelected by dependencies<AddressSelectionCallback>()

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
                        onAddressSelected.invoke("Airport Road")
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.address_selection),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }
}