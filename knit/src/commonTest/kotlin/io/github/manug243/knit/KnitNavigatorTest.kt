package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private data class Screen(val name: String) : NavKey

private enum class TestTab(override val root: NavKey) : KnitTab {
    Home(Screen("home")),
    Search(Screen("search")),
    Settings(Screen("settings")),
}

class KnitNavigatorTest {
    private fun navigator(initial: TestTab = TestTab.Home): KnitNavigator<TestTab> =
        KnitNavigator(
            tabs = TestTab.entries,
            stacks = TestTab.entries.associateWith { mutableListOf(it.root) },
            initial = initial,
        )

    @Test
    fun startsAtHomeRoot() {
        val navigator = navigator()
        assertEquals(TestTab.Home, navigator.currentTab)
        assertTrue(navigator.isAtTabRoot)
        assertEquals(listOf<NavKey>(Screen("home")), navigator.currentBackStack)
    }

    @Test
    fun navigateLeavesTabRoot() {
        val navigator = navigator()
        navigator.navigate(Screen("detail"))
        assertFalse(navigator.isAtTabRoot)
        assertEquals(listOf<NavKey>(Screen("home"), Screen("detail")), navigator.currentBackStack)
    }

    @Test
    fun selectingOtherTabKeepsStacks() {
        val navigator = navigator()
        navigator.navigate(Screen("detail"))
        navigator.select(TestTab.Search)
        assertEquals(TestTab.Search, navigator.currentTab)
        assertTrue(navigator.isAtTabRoot)
        navigator.select(TestTab.Home)
        assertEquals(listOf<NavKey>(Screen("home"), Screen("detail")), navigator.currentBackStack)
    }

    @Test
    fun selectingCurrentTabPopsToRoot() {
        val navigator = navigator()
        navigator.navigate(Screen("a"))
        navigator.navigate(Screen("b"))
        navigator.select(TestTab.Home)
        assertEquals(TestTab.Home, navigator.currentTab)
        assertEquals(listOf<NavKey>(Screen("home")), navigator.currentBackStack)
    }

    @Test
    fun backPopsCurrentStack() {
        val navigator = navigator()
        navigator.navigate(Screen("detail"))
        assertTrue(navigator.back())
        assertTrue(navigator.isAtTabRoot)
        assertEquals(TestTab.Home, navigator.currentTab)
    }

    @Test
    fun backFromOtherTabRootReturnsToHome() {
        val navigator = navigator()
        navigator.select(TestTab.Settings)
        assertTrue(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
    }

    @Test
    fun backFromOtherTabPopsBeforeReturningHome() {
        val navigator = navigator()
        navigator.select(TestTab.Search)
        navigator.navigate(Screen("result"))
        assertTrue(navigator.back())
        assertEquals(TestTab.Search, navigator.currentTab)
        assertTrue(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
    }

    @Test
    fun backAtHomeRootIsNotConsumed() {
        val navigator = navigator()
        assertFalse(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
        assertEquals(listOf<NavKey>(Screen("home")), navigator.currentBackStack)
    }

    @Test
    fun popNeverRemovesRoot() {
        val navigator = navigator()
        assertFalse(navigator.pop())
        assertEquals(1, navigator.currentBackStack.size)
    }

    @Test
    fun inactiveTabKeepsItsStackWhenBackReturnsHome() {
        val navigator = navigator()
        navigator.select(TestTab.Search)
        navigator.navigate(Screen("result"))
        navigator.select(TestTab.Settings)
        assertTrue(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
        assertEquals(listOf<NavKey>(Screen("search"), Screen("result")), navigator.backStack(TestTab.Search))
    }

    @Test
    fun canStartOnAnotherTab() {
        val navigator = navigator(initial = TestTab.Search)
        assertEquals(TestTab.Search, navigator.currentTab)
        assertTrue(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
    }

    @Test
    fun rejectsEmptyTabList() {
        assertFailsWith<IllegalArgumentException> {
            KnitNavigator<TestTab>(tabs = emptyList(), stacks = emptyMap(), initial = TestTab.Home)
        }
    }

    @Test
    fun navigateToTabSwitchesAndOpensKeyOnTop() {
        val navigator = navigator()
        navigator.navigate(TestTab.Search, Screen("result"))
        assertEquals(TestTab.Search, navigator.currentTab)
        assertEquals(listOf<NavKey>(Screen("search"), Screen("result")), navigator.currentBackStack)
        assertFalse(navigator.isAtTabRoot)
        assertEquals(listOf<NavKey>(Screen("home")), navigator.backStack(TestTab.Home))
    }

    @Test
    fun navigateToTabKeepsWhatTheTabAlreadyShowed() {
        val navigator = navigator()
        navigator.select(TestTab.Search)
        navigator.navigate(Screen("first"))
        navigator.select(TestTab.Home)
        navigator.navigate(TestTab.Search, Screen("second"))
        assertEquals(
            listOf<NavKey>(Screen("search"), Screen("first"), Screen("second")),
            navigator.backStack(TestTab.Search),
        )
        assertEquals(TestTab.Search, navigator.currentTab)
    }

    @Test
    fun navigateToCurrentTabJustPushes() {
        val navigator = navigator()
        navigator.navigate(TestTab.Home, Screen("detail"))
        assertEquals(listOf<NavKey>(Screen("home"), Screen("detail")), navigator.currentBackStack)
    }

    @Test
    fun backAfterDeepLinkPopsThenReturnsHome() {
        val navigator = navigator()
        navigator.navigate(TestTab.Settings, Screen("about"))
        assertTrue(navigator.back())
        assertEquals(TestTab.Settings, navigator.currentTab)
        assertTrue(navigator.back())
        assertEquals(TestTab.Home, navigator.currentTab)
    }

    @Test
    fun navigateToTheTopScreenDoesNotStackItTwice() {
        val navigator = navigator()
        navigator.navigate(Screen("detail"))
        navigator.navigate(Screen("detail"))
        assertEquals(listOf<NavKey>(Screen("home"), Screen("detail")), navigator.currentBackStack)

        // A deep link to the screen that is already open in the other tab only switches the tab.
        navigator.select(TestTab.Search)
        navigator.navigate(TestTab.Home, Screen("detail"))
        assertEquals(TestTab.Home, navigator.currentTab)
        assertEquals(listOf<NavKey>(Screen("home"), Screen("detail")), navigator.currentBackStack)
    }

    @Test
    fun navigateToUnknownTabIsRejected() {
        val navigator = KnitNavigator(
            tabs = listOf(TestTab.Home),
            stacks = mapOf(TestTab.Home to mutableListOf<NavKey>(TestTab.Home.root)),
            initial = TestTab.Home,
        )
        assertFailsWith<IllegalArgumentException> { navigator.navigate(TestTab.Search, Screen("x")) }
    }
}
