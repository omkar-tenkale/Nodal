package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.submit_button

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
        ui.draw {
            var selectedRoute by remember { mutableStateOf<SelectedRoute?>(null) }
            LaunchedEffect(Unit) {
                addRouteSelectionNode {
                    selectedRoute = it
                }
            }
            selectedRoute?.let {
                FlexibleBottomSheet(
                    onDismissRequest = {
                        removeSelf()
                    },
                    sheetState = alwaysIntermediatelyExpandedSheetState(),
                ) {
                    Column(Modifier.fillMaxSize()) {

                        Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
                            LaunchedEffect(Unit) {
                                addSelectedPaymentModeNode {
                                    selectedPaymentMode = it
                                }
                            }
                            childOrNull<SelectedPaymentModeNode>()?.Content()
                        }

                        Box(modifier = Modifier) {
                            LaunchedEffect(Unit) {
                                addRideSelectionNode {
                                    selectedRideType = it
                                }
                            }
                            childOrNull<RideSelectionNode>()?.Content()
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
                            contentScale = ContentScale.Crop,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}