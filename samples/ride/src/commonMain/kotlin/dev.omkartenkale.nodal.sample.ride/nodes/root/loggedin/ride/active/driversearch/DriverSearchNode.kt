@file:OptIn(ExperimentalResourceApi::class, ExperimentalResourceApi::class)

package dev.omkartenkale.nodal.sample.ride.nodes.root.loggedin.ride.active.driversearch

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.flexible.bottomsheet.material.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import dev.omkartenkale.nodal.Node
import dev.omkartenkale.nodal.compose.UI
import dev.omkartenkale.nodal.compose.draw
import dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet.alwaysIntermediatelyExpandedSheetState
import nodal.ride.generated.resources.Res
import nodal.ride.generated.resources.finding_driver
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
class DriverSearchNode() : Node() {

    override fun onAdded() {
        draw {
            FlexibleBottomSheet(
                onDismissRequest = {},
                sheetState = rememberFlexibleBottomSheetState()
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(Res.drawable.finding_driver),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )
            }
        }
    }
}