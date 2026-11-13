package io.github.manug243.knit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

/** Taps on a tab that is already selected, set by [KnitScaffold]. */
internal val LocalKnitReselects = staticCompositionLocalOf<Flow<KnitTab>?> { null }

/** The tab the screen belongs to, set by [KnitScaffold] for every screen. */
internal val LocalKnitEntryTab = staticCompositionLocalOf<KnitTab?> { null }

/**
 * Runs [onReselect] when the user taps the tab of this screen while it is already selected and
 * already on its start screen, so there is nothing left to pop. The usual use is scrolling a list to
 * the top:
 *
 * ```
 * val listState = rememberLazyListState()
 * KnitTabReselectEffect { listState.animateScrollToItem(0) }
 * ```
 *
 * When the tab shows a deeper screen, the tap pops back to the start screen as always and this does
 * not run. It only runs for screens of the tab that was tapped, and a new tap while [onReselect] is
 * still running cancels it and starts over. Outside a [KnitScaffold] it does nothing.
 */
@Composable
public fun KnitTabReselectEffect(onReselect: suspend () -> Unit) {
    val reselects = LocalKnitReselects.current ?: return
    val tab = LocalKnitEntryTab.current ?: return
    val currentOnReselect by rememberUpdatedState(onReselect)
    LaunchedEffect(reselects, tab) {
        reselects.filter { it == tab }.collectLatest { currentOnReselect() }
    }
}
