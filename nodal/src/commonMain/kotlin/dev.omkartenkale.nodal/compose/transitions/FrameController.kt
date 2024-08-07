package dev.omkartenkale.nodal.compose.transitions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier

/**
 * A stable object that processes changes to a [Backstack]'s list of screen keys, determining which
 * screens should be actively composed at any given time, and tweaking their appearance by applying
 * [Modifier]s.
 *
 * The [Backstack] composable will notify its controller whenever the backstack changes by calling
 * [updateBackstack], but the controller is in full control of when those changes actually get
 * reflected in the composition. For example, a controller may choose to keep some screens around
 * for a while, even after they're removed from the backstack, in order to animate their removal.
 */
@Stable
internal interface FrameController<T : Any> {

    /**
     * The frames that are currently being active. All active frames will be composed. When a frame
     * that is in the backstack stops appearing in this list, its state will be saved.
     *
     * Should be backed by either a [MutableState] or a [SnapshotStateList]. This property
     * will not be read until after [updateBackstack] is called at least once.
     */
    val activeFrames: List<BackstackFrame<T>>

    /**
     * Notifies the controller that a new backstack was passed in. This method must initialize
     * [activeFrames] first time it's called, and subsequently should probably result in
     * [activeFrames] being updated to show new keys or hide old ones, although the controller may
     * choose to do that later (e.g. if one of the active frames is currently being animated).
     *
     * This method will be called _directly from the composition_ – it must not perform side effects
     * or update any state that is not backed by snapshot state objects (such as [MutableState]s,
     * lists created by [mutableStateListOf], etc.).
     *
     * @param keys The latest backstack passed to [Backstack]. Will always contain at least one
     * element.
     */
    fun updateBackstack(keys: List<T>)

    /**
     * A frame controlled by a [FrameController], to be shown by [Backstack].
     */
    @Immutable
    data class BackstackFrame<out T : Any>(
        val key: T,
        val modifier: Modifier = Modifier
    )
}