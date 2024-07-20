package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.assigned.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.driver_arriving
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
class PrePickupNode() : Node(/*dismissible = false*/) {

    @OptIn(ExperimentalResourceApi::class)
    override fun onAdded() {
        draw {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = rememberFlexibleBottomSheetState(
                    confirmValueChange = {
                        it != FlexibleSheetValue.FullyExpanded
                    },
                    isModal = true
                )

            ) {
                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.driver_arriving),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }
    }
}