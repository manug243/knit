package io.github.manug243.knit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Navigation state for an app with tabs: one back stack per tab, Navigation 3 keys as destinations.
 *
 * Behavior:
 * - Selecting the current tab again pops it to its start screen. On the start screen itself it
 *   runs the [KnitTabReselectEffect] of the screen.
 * - [back] pops the current stack. On a tab's start screen it returns to the home tab,
 *   and on the home tab's start screen it returns false so the app may close.
 * - The stacks of inactive tabs are kept, so a screen opened in one tab survives a visit to another.
 */
@Stable
public class KnitNavigator<T : KnitTab> internal constructor(
    public val tabs: List<T>,
    private val stacks: Map<T, MutableList<NavKey>>,
    initial: T,
) {
    init {
        require(tabs.isNotEmpty()) { "KnitNavigator needs at least one tab" }
        require(initial in tabs) { "Initial tab $initial is not part of $tabs" }
    }

    /** The tab that is currently shown. */
    public var currentTab: T by mutableStateOf(initial)
        private set

    /** The first tab. Back from another tab's start screen leads here. */
    public val homeTab: T get() = tabs.first()

    /** Back stack of [tab]. Exposed so a `NavDisplay` can show it. */
    public fun backStack(tab: T): MutableList<NavKey> = stacks.getValue(tab)

    /** Back stack of the current tab. */
    public val currentBackStack: MutableList<NavKey> get() = backStack(currentTab)

    /** True on the start screen of the current tab. The native tab bar is only shown here. */
    public val isAtTabRoot: Boolean get() = currentBackStack.size <= 1

    // Carries a tap on the already selected tab to KnitTabReselectEffect. A flow because it is an
    // event, not state: a screen that appears later must not see a tap from before. No replay, and
    // tryEmit never suspends. A tap while a handler is still running cancels it and starts it again.
    internal val reselects = MutableSharedFlow<KnitTab>(extraBufferCapacity = 1)

    /**
     * Switches to [tab]. Selecting the current tab again returns to its start screen. When it is
     * already there, there is nothing to pop and [KnitTabReselectEffect] of the screen runs instead.
     */
    public fun select(tab: T) {
        require(tab in tabs) { "Unknown tab $tab" }
        if (tab == currentTab) {
            if (isAtTabRoot) reselects.tryEmit(tab) else popToRoot()
        } else {
            currentTab = tab
        }
    }

    /** Opens [key] on top of the current tab. Does nothing if [key] is already the top screen. */
    public fun navigate(key: NavKey) {
        push(currentBackStack, key)
    }

    // A key that is already on top is not added again. Two equal keys in a row would be the same
    // screen twice, which a double tap on a list row or a deep link to the open screen would cause.
    private fun push(stack: MutableList<NavKey>, key: NavKey) {
        if (stack.lastOrNull() != key) stack.add(key)
    }

    /**
     * Deep link into a tab: switches to [tab] without popping it and opens [key] on top of its
     * stack. Whatever the tab already showed stays below [key], so back leads there. If [key] is
     * already the top screen of [tab], it only switches the tab.
     */
    public fun navigate(tab: T, key: NavKey) {
        require(tab in tabs) { "Unknown tab $tab" }
        push(stacks.getValue(tab), key)
        currentTab = tab
    }

    /** Pops the current screen. Does nothing on a start screen. Returns whether something was popped. */
    public fun pop(): Boolean {
        val stack = currentBackStack
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Pops the current tab to its start screen. */
    public fun popToRoot() {
        val stack = currentBackStack
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    /**
     * Handles a system back. Returns true if the navigator consumed it,
     * false if nothing is left and the app may close.
     */
    public fun back(): Boolean = when {
        pop() -> true
        currentTab != homeTab -> {
            currentTab = homeTab
            true
        }
        else -> false
    }
}

/**
 * Creates and remembers a [KnitNavigator]. Back stacks and the selected tab survive configuration
 * changes and process death. [config] must register every [NavKey] subclass for polymorphic
 * serialization, which Navigation 3 requires on iOS.
 */
@Composable
public fun <T : KnitTab> rememberKnitNavigator(
    tabs: List<T>,
    config: SavedStateConfiguration,
    initial: T = tabs.first(),
): KnitNavigator<T> {
    val stacks = LinkedHashMap<T, MutableList<NavKey>>()
    for (tab in tabs) {
        key(tab) {
            stacks[tab] = rememberNavBackStack(config, tab.root)
        }
    }
    var savedIndex by rememberSaveable { mutableStateOf(tabs.indexOf(initial).coerceAtLeast(0)) }
    val navigator = remember(tabs) { KnitNavigator(tabs, stacks, tabs[savedIndex.coerceIn(tabs.indices)]) }
    savedIndex = tabs.indexOf(navigator.currentTab)
    return navigator
}
