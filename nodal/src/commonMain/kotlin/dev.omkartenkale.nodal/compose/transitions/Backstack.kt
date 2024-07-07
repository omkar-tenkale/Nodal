package dev.omkartenkale.nodal.compose.transitions


import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import dev.omkartenkale.nodal.compose.UI

/**
 * Identifies which direction a transition is being performed in.
 */
internal enum class TransitionDirection {
    Forward,
    Backward
}

/**
 * Fork of https://github.com/rjrjr/compose-backstack
 *
 * Renders the top of a stack of screens (as [T]s) and animates between screens when the top
 * value changes. Any state used by a screen will be preserved as long as it remains in the stack
 * (i.e. result of [remember] calls).
 *
 * The [backstack] must follow some rules:
 *  - Must always contain at least one item.
 *  - Items in the stack must implement `equals` and not change over the lifetime of the screen.
 *    If an item changes, it will be considered a new screen and any state held by the screen will
 *    be lost.
 *  - If items in the stack are reordered between compositions, the stack should not contain
 *    duplicates. If it does, due to how `@Pivotal` works, the states of those screens will be
 *    lost if they are moved around. If the list contains duplicates, an [IllegalArgumentException]
 *    will be thrown.
 *
 * This composable does not actually provide any navigation functionality – it just manages state,
 * and delegates to [FrameController]s to do things like animate screen transitions. It can be
 * plugged into your navigation library of choice, or just used on its own with a simple list of
 * screens.
 *
 * ## Saveable state caching
 *
 * Screens that contain persistable state using [rememberSaveable] will automatically have that
 * state saved when they are hidden, and restored the next time they're shown.
 *
 * ## Example
 *
 * ```
 * sealed class Screen {
 *   object ContactList: Screen()
 *   data class ContactDetails(val id: String): Screen()
 *   data class EditContact(val id: String): Screen()
 * }
 *
 * data class Navigator(
 *   val push: (Screen) -> Unit,
 *   val pop: () -> Unit
 * )
 *
 * @Composable fun App() {
 *   var backstack: List<Screen> by remember { mutableStateOf(listOf(Screen.ContactList)) }
 *   val navigator = remember {
 *     Navigator(
 *       push = { backstack += it },
 *       pop = { backstack = backstack.dropLast(1) }
 *     )
 *   }
 *
 *   Backstack(backstack) { screen ->
 *     when(screen) {
 *       Screen.ContactList -> ShowContactList(navigator)
 *       is Screen.ContactDetails -> ShowContact(screen.id, navigator)
 *       is Screen.EditContact -> ShowEditContact(screen.id, navigator)
 *     }
 *   }
 * }
 * ```
 *
 * @param backstack The stack of screen values.
 * @param modifier [Modifier] that will be applied to the container of screens. Neither affects nor
 * is affected by transition animations.
 * @param frameController The [FrameController] that manages things like transition animations.
 * Use [rememberTransitionController] for a reasonable default, or use the overload of this function
 * that takes a [BackstackTransition] instead.
 * @param content Called with each element of [backstack] to render it.
 */
@Composable
internal fun  Backstack(
    backstack: List<UI.Layer>,
    modifier: Modifier = Modifier,
    frameController: TransitionController,
) {

    // Notify the frame controller that the backstack has changed to allow it to do stuff like start
    // animating transitions. This call should eventually cause activeFrames to change, but that might
    // not happen immediately.
    //
    // Note: It's probably bad that this call is not done in a side effect. If the composition fails,
    // the controller won't know about it and will continue animating or whatever it was doing.
    // However, we do need to give the controller the chance to initialize itself with the initial
    // stack before we ask for its activeFrames, so this is a lazy way to do both that and subsequent
    // updates.
    frameController.updateBackstack(backstack)

    // Actually draw the screens.
    Box(modifier = modifier.clip(RectangleShape)) {
        // The frame controller is in complete control of what we actually show. The activeFrames
        // property should be backed by a snapshot state object, so this will recompose automatically
        // if the controller changes its frames.
        frameController.activeFrames.forEach { (item, frameControlModifier) ->
            // Even if screens are moved around within the list, as long as they're invoked through the
            // exact same sequence of source locations from within this key lambda, they will keep their
            // state.
            key(item) {
                // This call must be inside the key(){} wrapper.
                Box(frameControlModifier) {
                    item.Content()
                }
            }
        }
    }
}

/**
 * Renders the top of a stack of screens (as [T]s) and animates between screens when the top
 * value changes. Any state used by a screen will be preserved as long as it remains in the stack
 * (i.e. result of [remember] calls).
 *
 * See the documentation on [Backstack] for more information.
 *
 * @param backstack The stack of screen values.
 * @param modifier [Modifier] that will be applied to the container of screens. Neither affects nor
 * is affected by transition animations.
 * @param transition The [BackstackTransition] to use to animate screen transitions. For more,
 * call [rememberTransitionController] and pass it to the overload of this function that takes a
 * [FrameController] directly.
 * @param content Called with each element of [backstack] to render it.
 */
@Composable internal fun Backstack(
    backstack: List<UI.Layer>,
    modifier: Modifier = Modifier,
) {
    Backstack(backstack, modifier, rememberTransitionController())
}