package dev.omkartenkale.nodal.compose.transitions

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Defines transitions for a [Backstack]. Transitions control how screens are rendered by returning
 * [Modifier]s that will be used to wrap screen composables.
 *
 * @see Slide
 * @see Crossfade
 */
public fun interface BackstackTransition {

    /**
     * Returns a [Modifier] to use to draw screen in a [Backstack].
     *
     * @param visibility A float in the range `[0, 1]` that indicates at what visibility this screen
     * should be drawn. For example, this value will increase when [isTop] is true and the transition
     * is in the forward direction.
     * @param isTop True only when being called for the top screen. E.g. if the screen is partially
     * visible, then the top screen is always transitioning _out_, and non-top screens are either
     * transitioning out or invisible.
     */
    public fun Modifier.modifierForScreen(
        visibility: State<Float>,
        isTop: Boolean
    ): Modifier

    /**
     * A simple transition that slides screens horizontally.
     */
    public object Slide : BackstackTransition {
        override fun Modifier.modifierForScreen(
            visibility: State<Float>,
            isTop: Boolean
        ): Modifier = then(PercentageLayoutOffset(
            rawOffset = derivedStateOf { if (isTop) 1f - visibility.value else -1 + visibility.value }
        ))


        internal class PercentageLayoutOffset(private val rawOffset: State<Float>) :
            LayoutModifier {
            private val offset = { rawOffset.value.coerceIn(-1f..1f) }

            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(placeable.width, placeable.height) {
                    placeable.place(offsetPosition(IntSize(placeable.width, placeable.height)))
                }
            }

            internal fun offsetPosition(containerSize: IntSize) = IntOffset(
                // RTL is handled automatically by place.
                x = (containerSize.width * offset()).toInt(),
                y = 0
            )

            override fun toString(): String = "${this::class.simpleName}(offset=$offset)"
        }
    }

    /**
     * A simple transition that crossfades between screens.
     */
    public object Crossfade : BackstackTransition {
        override fun Modifier.modifierForScreen(
            visibility: State<Float>,
            isTop: Boolean
        ): Modifier = alpha(visibility.value)
    }

    /**
     * A simple transition that crossfades between screens.
     */
    public object None : BackstackTransition {
        override fun Modifier.modifierForScreen(
            visibility: State<Float>,
            isTop: Boolean
        ): Modifier = this
    }
}

/**
 * Convenience function to make it easier to make composition transitions.
 */
public fun BackstackTransition.modifierForScreen(
    visibility: State<Float>,
    isTop: Boolean
): Modifier = Modifier.modifierForScreen(visibility, isTop)