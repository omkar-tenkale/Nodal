package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.misc.Callback
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.driversearch.DriverSearchNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.assigned.pickup.PrePickupNode
import dev.omkartenkale.nodal.util.child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CompletedRide(val timeTaken: String)
class RideCompletedCallback(block: (CompletedRide) -> Unit) :
    Callback<CompletedRide> by Callback(block)

fun Node.addActiveRideNode(onRideCompleted: (CompletedRide) -> Unit) = addChild<ActiveRideNode>(
    RideCompletedCallback(onRideCompleted)
)

class ActiveRideNode : Node() {
    val rideCompletedCallback: RideCompletedCallback by dependencies()
    override fun onAdded() {
        coroutineScope.launch(Dispatchers.Main) {
            addChild<DriverSearchNode>()
            delay(5000)
            removeChild(child<DriverSearchNode>())
            addChild<PrePickupNode>()
            delay(5000)
            removeChild(child<PrePickupNode>())
            rideCompletedCallback(CompletedRide("5 min"))
        }
    }
}