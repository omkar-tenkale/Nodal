package dev.omkartenkale.nodal.compose.transitions

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

// https://easings.net/#easeOutExpo
internal val EaseOutExpoEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
public class TransitionSpec(
    public val topTransition: BackstackTransition,
    public val bottomTransition: BackstackTransition,
    public val bottomOnTop: Boolean,
    public val animationSpec: AnimationSpec<Float>,
){
    public companion object{
        public val Slide: TransitionSpec = TransitionSpec(
            topTransition = BackstackTransition.Top.HorizontalSlide,
            bottomTransition = BackstackTransition.Bottom.HorizontalSlide,
            bottomOnTop = false,
            animationSpec = TweenSpec(durationMillis = 750, easing = EaseOutExpoEasing),
        )
        public val BottomSheet: TransitionSpec = TransitionSpec(
            topTransition = BackstackTransition.Top.VerticalSlide,
            bottomTransition = BackstackTransition.Bottom.VerticalSlide,
            bottomOnTop = false,
            animationSpec = TweenSpec(durationMillis = 500, easing = EaseOutExpoEasing),
        )
        public val Fade: TransitionSpec = TransitionSpec(
            topTransition = BackstackTransition.Crossfade,
            bottomTransition = BackstackTransition.None,
            bottomOnTop = false,
            animationSpec = TweenSpec(durationMillis = 500, easing = EaseOutExpoEasing),
        )
        public val None: TransitionSpec = TransitionSpec(
            topTransition = BackstackTransition.None,
            bottomTransition = BackstackTransition.None,
            bottomOnTop = false,
            animationSpec = TweenSpec(durationMillis = 0),
        )
        public val Blur: TransitionSpec = TransitionSpec(
            topTransition = BackstackTransition.None,
            bottomTransition = BackstackTransition.Blur,
            bottomOnTop = false,
            animationSpec = TweenSpec(durationMillis = 500),
        )
    }
}

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

    public object Top {
        /**
         * A simple transition that slides screens horizontally.
         */
        public object HorizontalSlide : BackstackTransition {
            override fun Modifier.modifierForScreen(
                visibility: State<Float>,
                isTop: Boolean
            ): Modifier = background(
                Color.Black.copy(
                    alpha = lerp(
                        0f,
                        0.7f,
                        visibility.value
                    )
                )
            ) then (PercentageLayoutOffset(
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

        public object VerticalSlide : BackstackTransition {
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
                    x = 0,
                    y = (containerSize.height * offset()).toInt()
                )

                override fun toString(): String = "${this::class.simpleName}(offset=$offset)"
            }
        }


    }

    public object Bottom {
        public object HorizontalSlide : BackstackTransition {
            override fun Modifier.modifierForScreen(
                visibility: State<Float>,
                isTop: Boolean //isTop is always false
            ): Modifier = then (PercentageLayoutOffset(
                rawOffset = derivedStateOf { if (isTop) 1f - visibility.value else -1 * lerp(0.1f, 0f, visibility.value)}
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


        public object VerticalSlide : BackstackTransition {
            override fun Modifier.modifierForScreen(
                visibility: State<Float>,
                isTop: Boolean //isTop is always false
            ): Modifier = background(Color.Black).scale(lerp(0.95f, 1f, visibility.value) )
//                .then(PercentageLayoutOffset(
//                    rawOffset = derivedStateOf { if (isTop) 1f - visibility.value else  lerp(0.05f, 0f, visibility.value) }
//                ))
                .clip(RoundedCornerShape(10.dp))

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
                    x = 0,
                    y = (containerSize.height * offset()).toInt()
                )

                override fun toString(): String = "${this::class.simpleName}(offset=$offset)"
            }
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

    public object Blur : BackstackTransition {
        override fun Modifier.modifierForScreen(
            visibility: State<Float>,
            isTop: Boolean
        ): Modifier = blur(((1 - visibility.value) * 5).dp)
    }

    /**
     * No transition
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