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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val RailBreakpoint = 600.dp

private fun customTabBarState(
    items: List<ShellItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    wide: Boolean,
) = KnitTabBarState(
    items = items.mapIndexed { index, item ->
        KnitTabBarItem(item.title, item.icon, item.badge, selected = index == selectedIndex, onClick = { onSelect(index) })
    },
    wide = wide,
)

/** The icon of a tab with its badge, if it has one. */
@Composable
private fun BadgedIcon(item: ShellItem) {
    val badge = item.badge
    if (badge == null) {
        item.icon.Content()
    } else {
        // An empty badge is a plain dot.
        BadgedBox(badge = { if (badge.isEmpty()) Badge() else Badge { Text(badge) } }) {
            item.icon.Content()
        }
    }
}

/**
 * Material 3 chrome for Android, Desktop and Web: a bottom bar below [RailBreakpoint], a rail from there on.
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
    style: ShellStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val colors = style.colors
    val currentContent by rememberUpdatedState(content)
    val movableContent = remember {
        movableContentOf<PaddingValues> { safe ->
            ProvideKnitContentPadding(chrome = PaddingValues(0.dp), safe = safe) { currentContent() }
        }
    }
    val system = systemSafePadding()
    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }
    val layoutDirection = LocalLayoutDirection.current
    // What is left of the system insets for the screen. A visible bar handles the inset at its edge
    // itself, so the screen must not add it again.
    fun safePadding(wide: Boolean) = PaddingValues(
        start = if (wide && barVisible) 0.dp else system.calculateStartPadding(layoutDirection),
        top = if (topBar != null) 0.dp else system.calculateTopPadding(),
        end = system.calculateEndPadding(layoutDirection),
        bottom = if (!wide && barVisible) 0.dp else system.calculateBottomPadding(),
    )

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
                val custom = style.topBarContent
                if (custom != null) {
                    // A custom bar handles the status bar inset itself, like TopAppBar does.
                    custom(KnitTopBarState(bar.title, bar.actions, bar.backTitle != null, bar.onBack))
                } else {
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
                                IconButton(
                                    onClick = action.onClick,
                                    modifier = Modifier.semantics { contentDescription = action.label },
                                ) {
                                    action.icon.Content()
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.topBarContainer,
                            titleContentColor = colors.topBarContent,
                            navigationIconContentColor = colors.topBarContent,
                            actionIconContentColor = colors.topBarContent,
                        ),
                    )
                }
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
                    val custom = style.tabBar
                    if (custom != null) {
                        custom(customTabBarState(items, selectedIndex, onSelect, wide = true))
                    } else {
                        NavigationRail(containerColor = colors.tabBarContainer) {
                            items.forEachIndexed { index, item ->
                                NavigationRailItem(
                                    selected = index == selectedIndex,
                                    onClick = { onSelect(index) },
                                    icon = { BadgedIcon(item) },
                                    label = { Text(item.title) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = colors.tabBarSelectedContent,
                                        selectedTextColor = colors.tabBarSelectedContent,
                                        indicatorColor = colors.tabBarIndicator,
                                        unselectedIconColor = colors.tabBarContent,
                                        unselectedTextColor = colors.tabBarContent,
                                    ),
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    topBarSlot()
                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).then(contentArea)) {
                        movableContent(safePadding(wide = true))
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                topBarSlot()
                // The bar stays while the keyboard is open and the keyboard covers it. Its height counts as
                // already consumed, so an imePadding() in a screen leaves no gap above the keyboard and nothing
                // jumps when the keyboard opens or closes. The bar handles the navigation bar inset itself,
                // which this also covers. Until the bar is measured, the navigation bar inset stands in.
                val contentModifier = when {
                    !barVisible -> contentArea
                    barHeight > 0.dp -> contentArea.consumeWindowInsets(PaddingValues(bottom = barHeight))
                    else -> contentArea.consumeWindowInsets(NavigationBarDefaults.windowInsets)
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).then(contentModifier)) {
                    movableContent(safePadding(wide = false))
                }
                AnimatedVisibility(
                    visible = barVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                    modifier = Modifier.onSizeChanged { barHeight = with(density) { it.height.toDp() } },
                ) {
                    val custom = style.tabBar
                    if (custom != null) {
                        // A custom bar handles the navigation bar inset itself, like NavigationBar does.
                        custom(customTabBarState(items, selectedIndex, onSelect, wide = false))
                    } else {
                        NavigationBar(containerColor = colors.tabBarContainer) {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = index == selectedIndex,
                                    onClick = { onSelect(index) },
                                    icon = { BadgedIcon(item) },
                                    label = { Text(item.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.tabBarSelectedContent,
                                        selectedTextColor = colors.tabBarSelectedContent,
                                        indicatorColor = colors.tabBarIndicator,
                                        unselectedIconColor = colors.tabBarContent,
                                        unselectedTextColor = colors.tabBarContent,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
