package dev.omkartenkale.nodal.compose.transitions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import dev.omkartenkale.nodal.compose.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Returns the default [AnimationSpec] used for [rememberTransitionController].
 */
@Composable internal fun defaultBackstackAnimation(): AnimationSpec<Float> {
    return TweenSpec(durationMillis = 200)
}

/**
 * Returns a [FrameController] that will animate transitions between screens.
 *
 * @param transition The [BackstackTransition] that defines how to animate between screens when
 * the backstack changes. [BackstackTransition] contains a few simple pre-fab transitions.
 * @param animationSpec Defines the curve and speed of transition animations.
 * @param onTransitionStarting Callback that will be invoked before starting each transition.
 * @param onTransitionFinished Callback that will be invoked after each transition finishes.
 */
@Composable internal fun rememberTransitionController(
    transition: BackstackTransition = BackstackTransition.Slide,
    animationSpec: AnimationSpec<Float> = defaultBackstackAnimation(),
    onTransitionStarting: (from: List<UI.Layer>, to: List<UI.Layer>, TransitionDirection) -> Unit = { _, _, _ -> },
    onTransitionFinished: () -> Unit = {},
): TransitionController {
    val scope = rememberCoroutineScope()
    return remember { TransitionController(scope) }.also {
        it.transition = transition
        it.animationSpec = animationSpec
        it.onTransitionStarting = onTransitionStarting
        it.onTransitionFinished = onTransitionFinished

        LaunchedEffect(it) {
            it.runTransitionAnimations()
        }
    }
}

/**
 * A [FrameController] that implements transition modifiers specified by [BackstackTransition]s.
 *
 * @param scope The [CoroutineScope] used for animations.
 */
internal class TransitionController(
    private val scope: CoroutineScope
) : FrameController<UI.Layer> {

    /**
     * Holds information about an in-progress transition.
     */
    @Immutable
    private data class ActiveTransition<T : Any>(
        val fromFrame: FrameController.BackstackFrame<T>,
        val toFrame: FrameController.BackstackFrame<T>,
        val popping: Boolean
    )

    internal var transition: BackstackTransition? by mutableStateOf(null)
    internal var animationSpec: AnimationSpec<Float>? by mutableStateOf(null)
    internal var onTransitionStarting: ((from: List<UI.Layer>, to: List<UI.Layer>, TransitionDirection) -> Unit)?
            by mutableStateOf(null)
    internal var onTransitionFinished: (() -> Unit)? by mutableStateOf(null)

    /**
     * A snapshot of the backstack that will remain unchanged during transitions, even if
     * [updateBackstack] is called with a different stack. Just before
     * [starting a transition][animateTransition], this list will be used to determine if we should use
     * a forwards or backwards animation. It's a [MutableState] because it is used to derive the value
     * for [activeFrames], and so it needs to be observable.
     */
    private var displayedKeys: List<UI.Layer> by mutableStateOf(emptyList())

    /** The latest list of keys seen by [updateBackstack]. */
    private var targetKeys by mutableStateOf(emptyList<UI.Layer>())

    /**
     * Set to a non-null value only when actively animating between screens as the result of a call
     * to [updateBackstack]. This is a [MutableState] because it's used to derive the value of
     * [activeFrames], and so it needs to be observable.
     */
    private var activeTransition: ActiveTransition<UI.Layer>? by mutableStateOf(null)

    override val activeFrames: List<FrameController.BackstackFrame<UI.Layer>> by derivedStateOf {
        activeTransition?.let { transition ->
            if (transition.popping) {
                displayedKeys.dropLast(1).map { FrameController.BackstackFrame(it) } + listOf(transition.toFrame, transition.fromFrame)
            } else {
                displayedKeys.dropLast(2).map { FrameController.BackstackFrame(it) } + listOf(
                    transition.fromFrame,
                    transition.toFrame
                )
            }
        } ?: displayedKeys.map { FrameController.BackstackFrame(it) }
    }

    /**
     * Should be called from a coroutine that has access to a frame clock (i.e. from a
     * [rememberCoroutineScope] or in a [LaunchedEffect]), and must be allowed to run until this
     * [TransitionController] leaves the composition. It will never return unless cancelled.
     */
    suspend fun runTransitionAnimations() {
        // This flow handles backpressure by conflating: if targetKeys is changed multiple times while
        // an animation is running, we'll only get a single emission when it finishes.
        snapshotFlow { targetKeys }.collect { targetKeys ->
            if (displayedKeys.last() == targetKeys.last()) {
                // The visible screen didn't change, so we don't need to animate, but we need to update our
                // active list for the next time we check for navigation direction.
                displayedKeys = targetKeys
                return@collect
            }

            // The top of the stack was changed, so animate to the new top.
            animateTransition(fromKeys = displayedKeys, toKeys = targetKeys)
        }
    }

    override fun updateBackstack(keys: List<UI.Layer>) {
        // Always remember the latest stack, so if this call is happening during a transition we can
        // detect that when the transition finishes and start the next transition.
        targetKeys = keys

        // This is the first update, so we don't animate, and need to show the backstack as-is
        // immediately.
        if (displayedKeys.isEmpty()) {
            displayedKeys = keys
        }
    }

    /**
     * Called when [updateBackstack] gets a new backstack with a new top frame while idle, or after a
     * transition if the [targetKeys]' top is not [displayedKeys]' top.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun animateTransition(fromKeys: List<UI.Layer>, toKeys: List<UI.Layer>) {
        check(activeTransition == null) { "Can only start transitioning while idle." }

        val fromKey = fromKeys.last()
        val toKey = toKeys.last()
        val popping = toKey in fromKeys
        val progress = Animatable(0f)

        val fromVisibility = derivedStateOf { 1f - progress.value }
        val toVisibility = progress.asState()

        // Wrap modifier functions in each their own recompose scope so that if they read the visibility
        // (or any other state) directly, the modified node will actually be updated.
        val fromModifier = Modifier.composed {
            with(fromKey.transition) {
                modifierForScreen(fromVisibility, isTop = popping)
            }
        }
        val toModifier = Modifier.composed {
            with(toKey.transition) {
                modifierForScreen(toVisibility, isTop = !popping)
            }
        }

        activeTransition = ActiveTransition(
            fromFrame = FrameController.BackstackFrame(fromKey, fromModifier),
            toFrame = FrameController.BackstackFrame(toKey, toModifier),
            popping = popping
        )

        val oldActiveKeys = displayedKeys
        displayedKeys = targetKeys

        onTransitionStarting!!(oldActiveKeys, displayedKeys, if (popping) TransitionDirection.Backward else TransitionDirection.Forward)
        progress.animateTo(1f, animationSpec!!)
        activeTransition = null
        onTransitionFinished!!()
    }
}