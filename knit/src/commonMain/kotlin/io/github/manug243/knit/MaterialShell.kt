package io.github.manug243.knit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val RailBreakpoint = 600.dp

/**
 * Material 3 chrome for Android and Desktop: a bottom bar below [RailBreakpoint], a rail from there on.
 * The content moves between both layouts without losing its state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val currentContent by rememberUpdatedState(content)
    val movableContent = remember {
        movableContentOf {
            ProvideKnitContentPadding(PaddingValues(0.dp)) { currentContent() }
        }
    }

    // The bar content stays on screen while the bar animates out.
    val shownTopBar = remember { LastValue<ShellTopBar>() }
    if (topBar != null) shownTopBar.value = topBar
    // The top bar handles the status bar inset itself, so the screen below must not add it again.
    val topInsets = TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top)
    val contentArea = Modifier.then(if (topBar != null) Modifier.consumeWindowInsets(topInsets) else Modifier)
    val topBarSlot: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = topBar != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            shownTopBar.value?.let { bar ->
                CenterAlignedTopAppBar(
                    title = { Text(bar.title) },
                    navigationIcon = {
                        if (bar.backTitle != null) {
                            IconButton(onClick = bar.onBack) {
                                Icon(imageVector = KnitBackArrow, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        // The first action is the outermost one, like on iOS.
                        for (action in bar.actions.asReversed()) {
                            IconButton(onClick = action.onClick) {
                                Icon(imageVector = action.icon.vector, contentDescription = action.label)
                            }
                        }
                    },
                )
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= RailBreakpoint) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = barVisible,
                    enter = expandHorizontally(),
                    exit = shrinkHorizontally(),
                ) {
                    NavigationRail {
                        items.forEachIndexed { index, item ->
                            NavigationRailItem(
                                selected = index == selectedIndex,
                                onClick = { onSelect(index) },
                                icon = { Icon(imageVector = item.icon.vector, contentDescription = null) },
                                label = { Text(item.title) },
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    topBarSlot()
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).then(contentArea)) {
                        movableContent()
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                topBarSlot()
                // The bar handles the navigation bar inset itself, so nested scaffolds must not add it again.
                val contentModifier = if (barVisible) {
                    contentArea.consumeWindowInsets(NavigationBarDefaults.windowInsets)
                } else {
                    contentArea
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).then(contentModifier)) {
                    movableContent()
                }
                AnimatedVisibility(
                    visible = barVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = index == selectedIndex,
                                onClick = { onSelect(index) },
                                icon = { Icon(imageVector = item.icon.vector, contentDescription = null) },
                                label = { Text(item.title) },
                            )
                        }
                    }
                }
            }
        }
    }
}
