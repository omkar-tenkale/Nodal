package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.myaccoubt.MyAccountNode
import dev.omkartenkale.nodal.util.addChild
import dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.RideNode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.home
import nodal.ride.generated.resources.home_tabs
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

fun Node.addLoggedInNode(userName: String) = addChild<LoggedInNode>(LoggedInNode.Args(userName))

class LoggedInNode: Node() {
    class Args(val userName: String)
    private val args: Args by dependencies()

    @OptIn(ExperimentalResourceApi::class)
    override fun onAdded() {

        draw {

            println("LoggedInNode: Recompose")
            LaunchedEffect(Unit){
                println("LoggedInNode: LaunchedEffect")
            }

            Column {
                Image(
                    modifier = Modifier.fillMaxWidth().weight(1f).clickable {
                        addChild<RideNode>()
                    },
                    painter = painterResource(Res.drawable.home),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )

                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        addChild<MyAccountNode>()
                    },
                    painter = painterResource(Res.drawable.home_tabs),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }

//        childrenUpdatedEvents.onEach {
//            if(children.isEmpty()){
//                removeSelf()
//            }
//        }.launchIn(coroutineScope)

    }
}
