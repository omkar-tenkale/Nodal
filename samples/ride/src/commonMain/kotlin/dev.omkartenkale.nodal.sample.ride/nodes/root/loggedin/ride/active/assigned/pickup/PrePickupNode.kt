package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.assigned.pickup

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.driver_arriving
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.finding_driver

class PrePickupNode() : Node(/*dismissible = false*/) {

    @OptIn(ExperimentalResourceApi::class)
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
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.driver_arriving),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }
}