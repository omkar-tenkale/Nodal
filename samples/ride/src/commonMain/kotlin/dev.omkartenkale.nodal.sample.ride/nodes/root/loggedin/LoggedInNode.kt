package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin

import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.RideNode

fun Node.addLoggedInNode(userName: String) = addChild<LoggedInNode>(LoggedInNode.Args(userName))

class LoggedInNode: Node() {
    class Args(val userName: String)
    private val args: Args by dependencies()

    override fun onAdded() {
        addChild<RideNode>()
    }
}
