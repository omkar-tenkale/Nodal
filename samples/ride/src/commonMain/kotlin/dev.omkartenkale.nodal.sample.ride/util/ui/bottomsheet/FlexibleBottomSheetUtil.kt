package dev.omkartenkale.nodal.sample.ride.util.ui.bottomsheet

import androidx.compose.runtime.Composable
import com.skydoves.flexible.core.FlexibleSheetValue
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState

@Composable
fun nonExpandingSheetState() = rememberFlexibleBottomSheetState(
    confirmValueChange = {
        it != FlexibleSheetValue.FullyExpanded
    }
)

@Composable
fun alwaysIntermediatelyExpandedSheetState() = rememberFlexibleBottomSheetState(
    confirmValueChange = {
        it == FlexibleSheetValue.IntermediatelyExpanded
    }
)