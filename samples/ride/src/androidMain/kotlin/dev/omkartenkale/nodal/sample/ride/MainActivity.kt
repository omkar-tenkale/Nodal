package dev.omkartenkale.nodal.sample.ride

import android.os.Bundle
import dev.omkartenkale.nodal.NodalActivity
import dev.omkartenkale.nodal.sample.ride.nodes.root.RootNode

class MainActivity : NodalActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentNode<RootNode>()
    }
}