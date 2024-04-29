package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.nonExpandingSheetState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.Res
import org.jetbrains.kotlinx.multiplatform_library_template.ride.generated.resources.confirm_otp

@ExperimentalResourceApi
class ConfirmOtpNode : Node() {

    private val onOtpConfirmed: OTPConfirmedCallback by dependencies()

    override fun onAdded() {
        ui.draw {
            FlexibleBottomSheet(
                onDismissRequest = {
                    removeSelf()
                },
                sheetState = nonExpandingSheetState(),
                dragHandle = {}
            ) {
                Spacer(Modifier.height(12.dp))
                Image(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onOtpConfirmed("123456")
                    },
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(Res.drawable.confirm_otp),
                    contentDescription = null
                )
            }
        }
    }
}