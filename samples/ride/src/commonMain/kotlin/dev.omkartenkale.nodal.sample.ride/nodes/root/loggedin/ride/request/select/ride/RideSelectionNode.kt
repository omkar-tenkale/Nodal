@file:OptIn(ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.ride

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetSize
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.alwaysIntermediatelyExpandedSheetState
import dev.omkartenkale.nodal.util.addChild
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.ride_estimates


fun Node.addRideSelectionNode(rideSelectedCallback: (String) -> Unit) =
    addChild<RideSelectionNode>(RideBookedCallback { rideSelectedCallback(it) })

class RideBookedCallback(block: (String)-> Unit): Callback<String> by Callback(block)
class RideSelectionNode : Node() {
    val rideSelectedCallback by dependencies<RideBookedCallback>()

    @Composable
    fun Content() {
//        Column {
//            (1..10).forEach {
//                Box(modifier = Modifier.padding(5.dp).background(Color.Black).fillMaxWidth().height(50.dp).clickable {
//
//                })
//            }
//        }
        Image(
            modifier = Modifier.fillMaxWidth().clickable {
                rideSelectedCallback("Ride-123")
//                    sheetState.currentValue = FlexibleSheetValue.SlightlyExpanded
            },
            painter = painterResource(Res.drawable.ride_estimates),
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
    }
}