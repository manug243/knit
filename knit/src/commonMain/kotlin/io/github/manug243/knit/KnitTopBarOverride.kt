package io.github.manug243.knit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.navigation3.runtime.NavKey

/** The key of the screen that is currently composed, provided by [KnitScaffold] to every entry. */
internal val LocalKnitEntryKey = staticCompositionLocalOf<NavKey?> { null }

/** Top bars that screens declared themselves, by the key of the screen. Owned by [KnitScaffold]. */
internal val LocalKnitTopBarOverrides = staticCompositionLocalOf<SnapshotStateMap<NavKey, KnitTopBar>?> { null }

/**
 * Sets the top bar of the screen this is called from, for content that only the screen knows,
 * for example a title that comes from loaded data or actions that depend on the screen's state.
 * It wins over the `topBar` parameter of [KnitScaffold].
 *
 * Call it on every composition while the screen is shown. It has no effect outside a screen of a
 * [KnitScaffold]. The bar is remembered per [NavKey], so two entries with an equal key share it.
 */
@Composable
public fun KnitTopBarOverride(topBar: KnitTopBar) {
    val key = LocalKnitEntryKey.current ?: return
    val overrides = LocalKnitTopBarOverrides.current ?: return
    SideEffect { overrides[key] = topBar }
}
