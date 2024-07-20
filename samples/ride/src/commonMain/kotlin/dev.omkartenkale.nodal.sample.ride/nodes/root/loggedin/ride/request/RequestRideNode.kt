package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.paymentmode.SelectedPaymentModeNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.paymentmode.addSelectedPaymentModeNode
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.util.childOrNull
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.ride.RideSelectionNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.ride.addRideSelectionNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.route.SelectedRoute
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.select.route.addRouteSelectionNode
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.alwaysIntermediatelyExpandedSheetState
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import dev.omkartenkale.nodal.util.child
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.submit_button
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

data class RideRequest(
    val rideType: String,
    val selectedRoute: SelectedRoute,
    val selectedPaymentMode: String
)

class RequestRideCallback(block: (RideRequest) -> Unit) : Callback<RideRequest> by Callback(block)

fun Node.addRequestRideNode(onRideRequested: (RideRequest) -> Unit) =
    addChild<RequestRideNode>(RequestRideCallback(onRideRequested))

class RequestRideNode : Node() {
    val requestRideCallback by dependencies<RequestRideCallback>()

    var selectedRideType: String? = null
    var selectedPaymentMode: String? = null

    @OptIn(ExperimentalResourceApi::class)
    override fun onAdded() {
        draw(TransitionSpec.Fade) {
            var selectedRoute by remember { mutableStateOf<SelectedRoute?>(null) }
            LaunchedEffect(Unit) {
                addRouteSelectionNode {
                    selectedRoute = it
                }
            }
            selectedRoute?.let {
                Column {
                    FlexibleBottomSheet(
                        onDismissRequest = {
                            removeSelf()
                        },
                        sheetState = rememberFlexibleBottomSheetState(
                            confirmValueChange = {
                                it != FlexibleSheetValue.Hidden
                            },
                            isModal = true
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxSize()
                                .weight(1f, fill = true)
                                .background(Color.Red)
                        ) {

                            Box(modifier = Modifier) {
                                var rideSelectionNodeAdded by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    addRideSelectionNode {
                                        selectedRideType = it
                                    }
                                    rideSelectionNodeAdded = true
                                }
                                if (rideSelectionNodeAdded) {
                                    child<RideSelectionNode>().Content()
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                var selectedPaymentModeNodeAdded by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    addSelectedPaymentModeNode {
                                        selectedPaymentMode = it
                                    }
                                    selectedPaymentModeNodeAdded = true
                                }
                                if (selectedPaymentModeNodeAdded) {
                                    child<SelectedPaymentModeNode>().Content()
                                }
                            }
                            Image(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedRideType?.let { selectedRideType ->
                                        selectedPaymentMode?.let { selectedPaymentMode ->
                                            requestRideCallback(
                                                RideRequest(
                                                    selectedRideType,
                                                    it,
                                                    selectedPaymentMode
                                                )
                                            )
                                            removeSelf()
                                        }
                                    }
                                },
                                painter = painterResource(Res.drawable.submit_button),
                                contentScale = ContentScale.FillWidth,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}