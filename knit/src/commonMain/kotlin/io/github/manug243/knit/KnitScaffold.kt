package io.github.manug243.knit

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

private const val TabFadeMillis = 180

/**
 * Space that knit's chrome covers. Screens should pad their scrollable content by it, for example
 * as `contentPadding` of a `LazyColumn`.
 *
 * On iOS the native bars float above the content, so the bottom is the height of the tab bar and
 * the top the height of the navigation bar below the status bar, each while the bar is visible and
 * zero otherwise. On Android and Desktop the bars sit next to the content and this is zero.
 * The status bar inset itself is not part of the top value, add the system insets as usual.
 */
public val LocalKnitContentPadding: androidx.compose.runtime.ProvidableCompositionLocal<PaddingValues> =
    staticCompositionLocalOf { PaddingValues(0.dp) }

/**
 * Tab navigation with the platform's own chrome: a native `UITabBar` on iOS (Liquid Glass with the
 * iOS 26 SDK), a Material 3 navigation bar on Android, and a rail from 600 dp width.
 *
 * All screens live in one Compose tree and Navigation 3 stays the single source of truth. The bar is
 * only shown on the start screen of a tab and hides while the keyboard is open.
 *
 * @param navigator navigation state, see [rememberKnitNavigator].
 * @param tabs declares title and icon of every tab of the [navigator]. It runs on every
 * recomposition, so titles and icons can change at runtime.
 * @param entryProvider maps a [NavKey] to its screen, usually built with `entryProvider { ... }`.
 * @param topBar returns the top bar for the screen of a [NavKey], or null for a screen without one.
 * Below a tab's start screen the bar also shows a back button that pops the screen. It is called
 * with the top of the current tab's stack, so it follows navigation and can read the key's arguments.
 * @param colors colors of the Material bars on Android, Desktop and Web. The default follows the
 * surrounding `MaterialTheme`, see [KnitDefaults.colors]. iOS ignores it, its native bars take
 * their look from the system.
 * @param tabBar draws the tab bar yourself on Android, Desktop and Web, for example to match an own
 * design system. It gets the tabs and whether to draw a bottom bar or a side rail, and has to handle
 * the navigation bar inset itself. iOS ignores it and keeps the native bar.
 * @param topBarContent draws the top bar yourself on Android, Desktop and Web, from the title and
 * actions of [topBar]. It has to handle the status bar inset itself. iOS ignores it.
 */
@Composable
public fun <T : KnitTab> KnitScaffold(
    navigator: KnitNavigator<T>,
    tabs: KnitTabsScope<T>.() -> Unit,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
    modifier: Modifier = Modifier,
    topBar: (NavKey) -> KnitTopBar? = { null },
    colors: KnitColors = KnitDefaults.colors(),
    tabBar: (@Composable (KnitTabBarState) -> Unit)? = null,
    topBarContent: (@Composable (KnitTopBarState) -> Unit)? = null,
) {
    val items = buildTabItems(tabs)
    require(items.map { it.tab }.toSet() == navigator.tabs.toSet()) {
        "The tabs block has to declare exactly the tabs of the navigator: ${navigator.tabs}"
    }

    // Top bars that screens set themselves with KnitTopBarOverride. Dropped once the screen has left
    // every back stack.
    val overrides = remember { mutableStateMapOf<NavKey, KnitTopBar>() }
    // Tells every screen which key it shows, which KnitTopBarOverride needs.
    val currentEntryProvider by rememberUpdatedState(entryProvider)
    val keyedEntryProvider: (NavKey) -> NavEntry<NavKey> = remember {
        { navKey ->
            val entry = currentEntryProvider(navKey)
            NavEntry(navKey, contentKey = entry.contentKey, metadata = entry.metadata) {
                CompositionLocalProvider(LocalKnitEntryKey provides navKey) { entry.Content() }
            }
        }
    }
    SideEffect {
        val live = navigator.tabs.flatMapTo(HashSet()) { navigator.backStack(it) }
        overrides.keys.retainAll(live)
    }

    // Entries of every tab stay decorated, also while the tab is not shown. That keeps the
    // ViewModels and the saved state of an inactive tab alive.
    val entriesByTab = LinkedHashMap<T, List<NavEntry<NavKey>>>()
    for (tab in navigator.tabs) {
        key(tab) {
            entriesByTab[tab] = rememberDecoratedNavEntries(
                backStack = navigator.backStack(tab),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = keyedEntryProvider,
            )
        }
    }

    // Deeper screens are popped by NavDisplay. On a tab's start screen back leads to the home tab.
    val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = navigator.isAtTabRoot && navigator.currentTab != navigator.homeTab,
        onBackCompleted = { navigator.back() },
    )

    val hideRequests = remember { TabBarHideRequests() }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val barItems = items.map { ShellItem(it.title, it.icon) }
    val selectedIndex = items.indexOfFirst { it.tab == navigator.currentTab }

    val stack = navigator.currentBackStack
    val barFor: (NavKey) -> KnitTopBar? = { overrides[it] ?: topBar(it) }
    val topConfig = stack.lastOrNull()?.let(barFor)
    val shellTopBar = topConfig?.let { config ->
        ShellTopBar(
            title = config.title,
            actions = config.actions,
            backTitle = if (stack.size > 1) barFor(stack[stack.lastIndex - 1])?.title ?: "Back" else null,
            onBack = { navigator.back() },
        )
    }

    KnitPlatformShell(
        items = barItems,
        selectedIndex = selectedIndex,
        barVisible = navigator.isAtTabRoot && !imeVisible && hideRequests.count == 0,
        onSelect = { index -> navigator.select(items[index].tab) },
        topBar = shellTopBar,
        style = ShellStyle(colors, tabBar, topBarContent),
        modifier = modifier,
    ) {
        CompositionLocalProvider(
            LocalKnitTopBarOverrides provides overrides,
            LocalKnitTabBarHideRequests provides hideRequests,
        ) {
            // Switching tabs fades between the two screens, like the system tab bars do. It is not a
            // push or pop, so the content of a tab never slides.
            Crossfade(targetState = navigator.currentTab, animationSpec = tween(TabFadeMillis)) { tab ->
                NavDisplay(
                    entries = entriesByTab.getValue(tab),
                    onBack = { navigator.back() },
                )
            }
        }
    }
}

internal class ShellItem(
    val title: String,
    val icon: KnitIcon,
)

/**
 * Draws the platform's chrome around [content] and provides [LocalKnitContentPadding].
 * [selectedIndex] and [onSelect] refer to positions in [items].
 */
@Composable
internal expect fun KnitPlatformShell(
    items: List<ShellItem>,
    selectedIndex: Int,
    barVisible: Boolean,
    onSelect: (Int) -> Unit,
    topBar: ShellTopBar?,
    style: ShellStyle,
    modifier: Modifier,
    content: @Composable () -> Unit,
)

@Composable
internal fun ProvideKnitContentPadding(padding: PaddingValues, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalKnitContentPadding provides padding, content = content)
}
