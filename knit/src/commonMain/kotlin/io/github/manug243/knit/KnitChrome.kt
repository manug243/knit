package io.github.manug243.knit

import androidx.compose.foundation.layout.padding
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
