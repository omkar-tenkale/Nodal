package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
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
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import kotlinx.coroutines.launch
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.confirm_otp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@ExperimentalResourceApi
class ConfirmOtpNode : Node() {

    private val onOtpConfirmed: OTPConfirmedCallback by dependencies()

    override fun onAdded() {
         draw(TransitionSpec.None) {
//             Box(Modifier.background(Color.Red).fillMaxSize()) {
                 val scope = rememberCoroutineScope()
                 val state = nonExpandingSheetState()
                 FlexibleBottomSheet(
                     onDismissRequest = {
                         removeSelf()
                         scope.launch {
                             state.hide()
                         }
                     },
                     sheetState = state,
                     dragHandle = {}
                 ) {
                     Spacer(Modifier.height(12.dp))
                     Box {
                         Image(
                             modifier = Modifier.fillMaxWidth(),
                             contentScale = ContentScale.FillWidth,
                             painter = painterResource(Res.drawable.confirm_otp),
                             contentDescription = null
                         )
                         Box(modifier = Modifier.padding(30.dp).size(50.dp).clickable {
                             removeSelf()
                         }.align(Alignment.BottomStart))
                         Box(modifier = Modifier.padding(30.dp).size(50.dp).clickable {
                             onOtpConfirmed("123456")
                         }.align(Alignment.BottomEnd))
                     }
                 }
//             }
        }
    }
}