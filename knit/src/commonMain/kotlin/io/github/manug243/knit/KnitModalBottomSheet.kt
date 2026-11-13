package io.github.manug243.knit

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A Material 3 modal bottom sheet that also hides the tab bar for as long as it is shown. On iOS the
 * native bar floats above everything Compose draws, so with a plain `ModalBottomSheet` it would sit
 * on top of the sheet. This is the sheet and [KnitTabBarHidden] in one call:
 *
 * ```
 * if (showSheet) {
 *     KnitModalBottomSheet(onDismissRequest = { showSheet = false }) { Text("Hello") }
 * }
 * ```
 *
 * Like the Material sheet it is shown while it is in the composition. It keeps the Material
 * defaults. Need more control, for example a sheet state? Use `ModalBottomSheet` and
 * [KnitTabBarHidden] yourself.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun KnitModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    KnitTabBarHidden()
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier, content = content)
}
