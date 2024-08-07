@file:OptIn(ExperimentalResourceApi::class, ExperimentalResourceApi::class)

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
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.alwaysIntermediatelyExpandedSheetState
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import dev.omkartenkale.nodal.util.addChild
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.route_selection
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

data class SelectedRoute(val pickup: SelectedPickup, val destination: SelectedDestination)
data class SelectedPickup(val address: String, val coordinates: Float)
data class SelectedDestination(val address: String, val coordinates: Float)
class RouteSelectionCallback(block: (SelectedRoute)-> Unit): Callback<SelectedRoute> by Callback(block)
fun Node.addRouteSelectionNode(onRouteSelected: (SelectedRoute)-> Unit) = addChild<RouteSelectionNode>(
    RouteSelectionCallback(onRouteSelected)
)

class RouteSelectionNode : Node() {

    private val onRouteSelected by dependencies<RouteSelectionCallback>()

    @OptIn(ExperimentalResourceApi::class)
    override fun onAdded() {
        draw(TransitionSpec.None) {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = alwaysIntermediatelyExpandedSheetState(),
            ) {
                Image(
                    modifier = Modifier.clickable {
                        onRouteSelected.invoke(
                            SelectedRoute(
                                SelectedPickup("Airport",0f),
                                SelectedDestination("Home",0f)
                            )
                        )
                        removeSelf()
                    },
                    painter = painterResource(Res.drawable.route_selection),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }
    }
}