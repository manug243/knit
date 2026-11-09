package io.github.manug243.knit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    modifier: Modifier,
    content: @Composable () -> Unit,
) = MaterialShell(items, selectedIndex, barVisible, onSelect, topBar, modifier, content)
