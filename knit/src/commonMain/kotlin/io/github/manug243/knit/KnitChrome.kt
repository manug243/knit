package io.github.manug243.knit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/** Counts the screens and overlays that asked for a hidden tab bar. Owned by [KnitScaffold]. */
internal class TabBarHideRequests {
    var count: Int by mutableIntStateOf(0)
}

internal val LocalKnitTabBarHideRequests = staticCompositionLocalOf<TabBarHideRequests?> { null }

/**
 * Keeps the tab bar hidden for as long as this is in the composition. Use it next to a modal bottom
 * sheet or a full-width dialog: on iOS the native bar floats above everything Compose draws, so
 * without this it would stay on top of the sheet. Has no effect outside a [KnitScaffold].
 *
 * ```
 * if (showSheet) {
 *     KnitTabBarHidden()
 *     ModalBottomSheet(onDismissRequest = { showSheet = false }) { ... }
 * }
 * ```
 */
@Composable
public fun KnitTabBarHidden() {
    val requests = LocalKnitTabBarHideRequests.current ?: return
    DisposableEffect(requests) {
        requests.count++
        onDispose { requests.count-- }
    }
}

/** Set by the platform shells, null outside a [KnitScaffold]. */
internal val LocalKnitSafePadding = staticCompositionLocalOf<PaddingValues?> { null }

/** Status bar, navigation bar and display cutout. The keyboard is left to `imePadding()`. */
@Composable
internal fun systemSafePadding(): PaddingValues =
    WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues()

/**
 * Everything a screen has to keep clear on the current platform, in one value: the system bars, the
 * display cutout and knit's own bars. A screen that uses it needs no platform checks and no inset
 * math of its own.
 *
 * ```
 * LazyColumn(contentPadding = knitSafePadding()) { ... }
 * ```
 *
 * Passed as `contentPadding`, a list still scrolls behind the bars, which is what makes the glass
 * bars on iOS look right. Where a bar of knit already handles a system inset, as the Material bars
 * on Android do, that inset is not counted again. The keyboard is not part of it, use `imePadding()`.
 * Outside a [KnitScaffold] these are just the system insets.
 */
@Composable
public fun knitSafePadding(): PaddingValues = LocalKnitSafePadding.current ?: systemSafePadding()

/**
 * Keeps this clear of the system bars, the display cutout and knit's bars, see [knitSafePadding].
 * For screens that do not scroll, and for things at the edge of the screen like a snackbar host.
 * The insets count as consumed, so an `imePadding()` further inside does not add them a second time.
 *
 * ```
 * Column(Modifier.fillMaxSize().knitSafeArea()) { ... }
 * ```
 */
@Composable
public fun Modifier.knitSafeArea(): Modifier {
    val padding = knitSafePadding()
    return padding(padding).consumeWindowInsets(padding)
}

/**
 * Pads this by the space knit's chrome covers, see [LocalKnitContentPadding]. Put it on things that
 * sit at the edge of the screen, such as a snackbar host, so the native bars on iOS do not cover them.
 * It is a no-op where the bars sit next to the content.
 *
 * ```
 * SnackbarHost(snackbarState, modifier = Modifier.knitContentPadding())
 * ```
 */
@Composable
public fun Modifier.knitContentPadding(): Modifier = padding(LocalKnitContentPadding.current)
