@file:OptIn(ExperimentalResourceApi::class, ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.driversearch

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.alwaysIntermediatelyExpandedSheetState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.finding_driver
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.google_map

class DriverSearchNode() : Node() {

    override fun onAdded() {
        ui.draw {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = alwaysIntermediatelyExpandedSheetState(),
            ) {
                Image(
                    modifier = Modifier.clickable {
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.finding_driver),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }
}