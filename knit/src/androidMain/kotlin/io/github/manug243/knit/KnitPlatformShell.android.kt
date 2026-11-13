package io.github.manug243.knit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    // Only matters where the bars float above the content.
    @Suppress("UNUSED_PARAMETER") barAllowed: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    style: ShellStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) = MaterialShell(items, selectedIndex, barVisible, onSelect, topBar, style, modifier, content)
