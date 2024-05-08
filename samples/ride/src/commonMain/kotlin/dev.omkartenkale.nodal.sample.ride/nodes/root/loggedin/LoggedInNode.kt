package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.RideNode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun Node.addLoggedInNode(userName: String) = addChild<LoggedInNode>(LoggedInNode.Args(userName))

class LoggedInNode: Node() {
    class Args(val userName: String)
    private val args: Args by dependencies()

    override fun onAdded() {
        childrenUpdatedEvents.onEach {
            if(children.isEmpty()){
                removeSelf()
            }
        }.launchIn(coroutineScope)
        addChild<RideNode>()
    }
}
