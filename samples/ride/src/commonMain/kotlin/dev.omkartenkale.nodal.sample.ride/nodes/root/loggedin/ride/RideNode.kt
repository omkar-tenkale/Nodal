@file:OptIn(ExperimentalResourceApi::class, ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.compose.transitions.BackstackTransition
import dev.omkartenkale.nodal.compose.transitions.TransitionSpec
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.ActiveRideNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.CompletedRide
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.addActiveRideNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.completed.addDriverRatingNode
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.RideRequest
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.request.addRequestRideNode
import dev.omkartenkale.nodal.util.isAdded
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.google_map
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.seconds

class RideNode: Node() {

    private lateinit var layer: UI.Layer

    override fun onAdded() {
//        childrenUpdatedEvents.onEach {
//            if(children.isEmpty()){
//                removeSelf()
//            }
//        }.launchIn(coroutineScope)
        layer = ui.draw(TransitionSpec.Fade) {
            Image(
                modifier = it.clickable {
                    removeSelf()
                },
                painter = painterResource(Res.drawable.google_map),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            LaunchedEffect(Unit){
                val rideActive = false // Random.nextBoolean()

                if(rideActive && isAdded){
                    addActiveRideNode(::onRideCompleted)
                }else{
                    addRequestRideNode(::onRideRequested)
                }
            }
        }
    }

    private fun onRideRequested(rideRequest: RideRequest){
        addActiveRideNode(::onRideCompleted)
    }

    private fun onRideCompleted(completedRide: CompletedRide){
        addDriverRatingNode{
            addRequestRideNode(::onRideRequested)
        }
        children.filterIsInstance<ActiveRideNode>().forEach {
            removeChild(it)
        }
    }

    override fun onRemoved() {
        layer.destroy()
    }
}