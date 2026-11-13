package io.github.manug243.knit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class Page(val name: String) : NavKey

private val ScaffoldConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Page::class, Page.serializer())
        }
    }
}

private enum class ScaffoldTab(override val root: NavKey) : KnitTab {
    A(Page("a")),
    B(Page("b")),
}

private val scaffoldIcon = KnitIcon(
    sfSymbol = "circle",
    vector = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(),
)

@OptIn(ExperimentalTestApi::class)
class KnitScaffoldTest {
    private fun androidx.compose.ui.test.ComposeUiTest.show(onNavigator: (KnitNavigator<ScaffoldTab>) -> Unit) {
        setContent {
            val navigator = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            onNavigator(navigator)
            KnitScaffold(
                navigator = navigator,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        var count by rememberSaveable { mutableIntStateOf(0) }
                        Column {
                            Text("page ${page.name}")
                            Text("count ${page.name}=$count")
                            Button(onClick = { count++ }) { Text("inc ${page.name}") }
                            Button(onClick = { navigator.navigate(Page("${page.name}-detail")) }) {
                                Text("open ${page.name}")
                            }
                        }
                    }
                },
            )
        }
    }

    @Test
    fun showsHomeTabFirst() = runComposeUiTest {
        show {}
        onNodeWithText("page a").assertExists()
        onNodeWithText("page b").assertDoesNotExist()
        onNodeWithText("Tab A").assertExists()
        onNodeWithText("Tab B").assertExists()
    }

    @Test
    fun tappingBarSwitchesTab() = runComposeUiTest {
        show {}
        onNodeWithText("Tab B").performClick()
        waitForIdle()
        onNodeWithText("page b").assertExists()
        onNodeWithText("page a").assertDoesNotExist()
    }

    @Test
    fun inactiveTabKeepsScreenState() = runComposeUiTest {
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        show { navigator = it }
        onNodeWithText("inc a").performClick()
        onNodeWithText("inc a").performClick()
        onNodeWithText("count a=2").assertExists()

        runOnIdle { navigator.select(ScaffoldTab.B) }
        onNodeWithText("page b").assertExists()
        runOnIdle { navigator.select(ScaffoldTab.A) }
        onNodeWithText("count a=2").assertExists()
    }

    @Test
    fun barHidesBelowTabRootAndBackReturns() = runComposeUiTest {
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        show { navigator = it }
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("page a-detail").assertExists()
        onNodeWithText("Tab A").assertDoesNotExist()

        runOnIdle { assertEquals(true, navigator.back()) }
        waitForIdle()
        onNodeWithText("page a").assertExists()
        onNodeWithText("Tab A").assertExists()
    }

    @Test
    fun topBarShowsTitleAndBackPopsScreen() = runComposeUiTest {
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            navigator = nav
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) { Text("open ${page.name}") }
                    }
                },
                topBar = { key -> if (key is Page && key.name.endsWith("-detail")) KnitTopBar("title ${key.name}") else null },
            )
        }
        onNodeWithText("title a-detail").assertDoesNotExist()
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("title a-detail").assertExists()
        onNodeWithContentDescription("Back").assertExists()

        onNodeWithContentDescription("Back").performClick()
        waitForIdle()
        onNodeWithText("title a-detail").assertDoesNotExist()
        onNodeWithText("open a").assertExists()
        runOnIdle { assertEquals(true, navigator.isAtTabRoot) }
    }

    @Test
    fun topBarActionIsShownAndClickable() = runComposeUiTest {
        var clicks = 0
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) { Text("open ${page.name}") }
                    }
                },
                topBar = { key ->
                    if (key is Page && key.name.endsWith("-detail")) {
                        KnitTopBar("detail", actions = listOf(KnitAction("Share", scaffoldIcon) { clicks++ }))
                    } else {
                        null
                    }
                },
            )
        }
        onNodeWithContentDescription("Share").assertDoesNotExist()
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithContentDescription("Share").performClick()
        onNodeWithContentDescription("Share").performClick()
        runOnIdle { assertEquals(2, clicks) }
    }

    @Test
    fun screenCanSetItsOwnTopBar() = runComposeUiTest {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        if (page.name.endsWith("-detail")) {
                            var count by remember { mutableIntStateOf(0) }
                            KnitTopBarOverride(KnitTopBar("loaded $count"))
                            Button(onClick = { count++ }) { Text("load") }
                        } else {
                            Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) { Text("open ${page.name}") }
                        }
                    }
                },
            )
        }
        onNodeWithText("loaded 0").assertDoesNotExist()
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("loaded 0").assertExists()
        onNodeWithText("load").performClick()
        waitForIdle()
        onNodeWithText("loaded 1").assertExists()
        onNodeWithText("loaded 0").assertDoesNotExist()
    }

    @Test
    fun tabBarHidesWhileRequested() = runComposeUiTest {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> {
                        var sheet by remember { mutableStateOf(false) }
                        if (sheet) KnitTabBarHidden()
                        Button(onClick = { sheet = !sheet }) { Text("toggle sheet") }
                    }
                },
            )
        }
        onNodeWithText("Tab A").assertExists()
        onNodeWithText("toggle sheet").performClick()
        waitForIdle()
        onNodeWithText("Tab A").assertDoesNotExist()
        onNodeWithText("toggle sheet").performClick()
        waitForIdle()
        onNodeWithText("Tab A").assertExists()
    }

    @Test
    fun customTabBarReplacesMaterialBar() = runComposeUiTest {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider { entry<Page> { page -> Text("page ${page.name}") } },
                tabBar = { state ->
                    Row {
                        for (item in state.items) {
                            Button(onClick = item.onClick) {
                                Text("own ${item.title} ${if (item.selected) "on" else "off"}")
                            }
                        }
                    }
                },
            )
        }
        onNodeWithText("Tab A").assertDoesNotExist()
        onNodeWithText("own Tab A on").assertExists()
        onNodeWithText("own Tab B off").performClick()
        waitForIdle()
        onNodeWithText("page b").assertExists()
        onNodeWithText("own Tab B on").assertExists()
    }

    @Test
    fun customTopBarGetsTitleActionsAndBack() = runComposeUiTest {
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            navigator = nav
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) { Text("open ${page.name}") }
                    }
                },
                topBar = { key ->
                    if (key is Page && key.name.endsWith("-detail")) {
                        KnitTopBar("detail", actions = listOf(KnitAction("Share", scaffoldIcon) {}))
                    } else {
                        null
                    }
                },
                topBarContent = { state ->
                    Row {
                        Text("own ${state.title} actions=${state.actions.size}")
                        if (state.showBack) Button(onClick = state.onBack) { Text("own back") }
                    }
                },
            )
        }
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("own detail actions=1").assertExists()
        onNodeWithContentDescription("Back").assertDoesNotExist()
        onNodeWithText("own back").performClick()
        waitForIdle()
        runOnIdle { assertEquals(true, navigator.isAtTabRoot) }
    }

    @Test
    fun tabsBlockIsComposableAndIconsCanBeComposables() = runComposeUiTest {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            // A composable reads the title here, like stringResource would.
            val titleA = remember { "read in composition A" }
            val ownIcon = KnitIcon("circle") { Text("own icon") }
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, titleA, ownIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider { entry<Page> { page -> Text("page ${page.name}") } },
            )
        }
        // The tab item merges its icon and label, so the icon is only found in the unmerged tree.
        onNodeWithText("read in composition A", useUnmergedTree = true).assertExists()
        onNodeWithText("own icon", useUnmergedTree = true).assertExists()
    }

    @Test
    fun screensGetSafePaddingAndCanUseSafeArea() = runComposeUiTest {
        var insideScaffold: PaddingValues? = null
        setContent {
            val navigator = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = navigator,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        insideScaffold = knitSafePadding()
                        Text("page ${page.name}", modifier = Modifier.knitSafeArea())
                    }
                },
            )
        }
        onNodeWithText("page a").assertExists()
        // The desktop test window has no system insets and the Material bars sit next to the content.
        assertEquals(PaddingValues(0.dp), insideScaffold)
    }

    @Test
    fun safePaddingWorksOutsideAScaffold() = runComposeUiTest {
        setContent { Text("alone", modifier = Modifier.knitSafeArea()) }
        onNodeWithText("alone").assertExists()
    }

    @Test
    fun floatingBarsPadEveryScreenByItsOwnBars() = runComposeUiTest {
        // Stands in for the iOS shell, the only one with floating bars.
        val floating = ShellPadding { hasTopBar, isTabRoot ->
            val padding = PaddingValues(top = if (hasTopBar) 44.dp else 0.dp, bottom = if (isTabRoot) 83.dp else 0.dp)
            EntryPaddingValues(chrome = padding, safe = padding)
        }
        val seen = mutableMapOf<String, MutableSet<PaddingValues>>()
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        setContent {
            navigator = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            CompositionLocalProvider(LocalShellPadding provides floating) {
                KnitScaffold(
                    navigator = navigator,
                    tabs = {
                        tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                        tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                    },
                    entryProvider = entryProvider {
                        entry<Page> { page ->
                            seen.getOrPut(page.name) { mutableSetOf() } += knitSafePadding()
                            Text("page ${page.name}")
                        }
                    },
                    topBar = { key -> if ((key as Page).name == "detail") KnitTopBar("Detail") else null },
                )
            }
        }
        runOnIdle { navigator.navigate(Page("detail")) }
        onNodeWithText("page detail").assertExists()

        // The start screen keeps its padding while the detail screen and its top bar come in.
        assertEquals(setOf(PaddingValues(top = 0.dp, bottom = 83.dp)), seen.getValue("a").toSet())
        assertEquals(setOf(PaddingValues(top = 44.dp, bottom = 0.dp)), seen.getValue("detail").toSet())
    }

    @Test
    fun noTopBarWithoutConfig() = runComposeUiTest {
        show {}
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("page a-detail").assertExists()
        onNodeWithContentDescription("Back").assertDoesNotExist()
    }

    @Test
    fun detailStackOfInactiveTabSurvives() = runComposeUiTest {
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        show { navigator = it }
        onNodeWithText("open a").performClick()
        waitForIdle()
        runOnIdle { navigator.select(ScaffoldTab.B) }
        waitForIdle()
        onNodeWithText("page b").assertExists()
        runOnIdle { navigator.select(ScaffoldTab.A) }
        waitForIdle()
        onNodeWithText("page a-detail").assertExists()
    }

    @Test
    fun badgeIsShownAndUpdated() = runComposeUiTest {
        var badge by mutableStateOf<String?>("3")
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon, badge = badge)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider { entry<Page> { Text("page ${it.name}") } },
            )
        }
        onNodeWithText("3", useUnmergedTree = true).assertExists()
        badge = "12"
        waitForIdle()
        onNodeWithText("3", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithText("12", useUnmergedTree = true).assertExists()
        badge = null
        waitForIdle()
        onNodeWithText("12", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun customTabBarGetsBadge() = runComposeUiTest {
        var seen: List<String?> = emptyList()
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon, badge = "7")
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider { entry<Page> { Text("page ${it.name}") } },
                tabBar = { state -> seen = state.items.map { it.badge } },
            )
        }
        runOnIdle { assertEquals(listOf<String?>("7", null), seen) }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.showReselect(
        fired: MutableList<String>,
        onNavigator: (KnitNavigator<ScaffoldTab>) -> Unit,
    ) {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            onNavigator(nav)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> { page ->
                        KnitTabReselectEffect { fired += page.name }
                        Column {
                            Text("page ${page.name}")
                            Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) {
                                Text("open ${page.name}")
                            }
                        }
                    }
                },
            )
        }
    }

    @Test
    fun reselectingTheTabAtItsStartScreenRunsTheEffect() = runComposeUiTest {
        val fired = mutableListOf<String>()
        showReselect(fired) {}
        onNodeWithText("Tab A").performClick()
        waitForIdle()
        assertEquals(listOf("a"), fired)
        onNodeWithText("Tab A").performClick()
        waitForIdle()
        assertEquals(listOf("a", "a"), fired)
    }

    @Test
    fun switchingTabsDoesNotRunTheEffectAndOnlyTheTappedTabReacts() = runComposeUiTest {
        val fired = mutableListOf<String>()
        showReselect(fired) {}
        onNodeWithText("Tab B").performClick()
        waitForIdle()
        assertEquals(emptyList(), fired)
        onNodeWithText("Tab B").performClick()
        waitForIdle()
        assertEquals(listOf("b"), fired)
    }

    @Test
    fun reselectingPopsADetailScreenInsteadOfRunningTheEffect() = runComposeUiTest {
        val fired = mutableListOf<String>()
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        showReselect(fired) { navigator = it }
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("page a-detail").assertExists()

        // The bar is hidden below a start screen, so the tap on the tab comes from the navigator.
        runOnIdle { navigator.select(ScaffoldTab.A) }
        waitForIdle()
        onNodeWithText("page a").assertExists()
        assertEquals(emptyList(), fired)
    }

    // A new provider instance on every call, like an app that builds it inline in its content.
    private fun entryProviderCounting(
        onCall: () -> Unit,
        content: @androidx.compose.runtime.Composable (Page) -> Unit,
    ): (NavKey) -> androidx.navigation3.runtime.NavEntry<NavKey> {
        val inner = entryProvider<NavKey> { entry<Page> { content(it) } }
        return { key -> onCall(); inner(key) }
    }

    @Test
    fun screenSurvivesRecompositionsOfTheScaffold() = runComposeUiTest {
        var badge by mutableStateOf<String?>(null)
        var title by mutableStateOf("first")
        var created = 0
        var providerCalls = 0
        lateinit var navigator: KnitNavigator<ScaffoldTab>
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            navigator = nav
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon, badge = badge)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProviderCounting(onCall = { providerCalls++ }) { page ->
                    // Not saveable on purpose: it is lost when the screen is recreated.
                    var count by remember { mutableIntStateOf(0) }
                    remember { created++ }
                    Column {
                        Text("page ${page.name}")
                        Text("count ${page.name}=$count")
                        Button(onClick = { count++ }) { Text("inc ${page.name}") }
                        Button(onClick = { nav.navigate(Page("${page.name}-detail")) }) {
                            Text("open ${page.name}")
                        }
                    }
                },
                topBar = { KnitTopBar(title = "$title ${(it as Page).name}") },
            )
        }
        onNodeWithText("open a").performClick()
        waitForIdle()
        onNodeWithText("inc a-detail").performClick()
        onNodeWithText("count a-detail=1").assertExists()
        val createdBefore = created
        val callsBefore = providerCalls

        badge = "1"
        waitForIdle()
        title = "second"
        waitForIdle()
        badge = "2"
        waitForIdle()

        onNodeWithText("count a-detail=1").assertExists()
        assertEquals(createdBefore, created, "a screen was recreated by an unrelated recomposition")
        assertEquals(callsBefore, providerCalls, "the entry provider ran again for an unchanged back stack")
    }

    @Test
    fun bothScreensAreComposedWhenAPushStarts() = runComposeUiTest {
        show {}
        mainClock.autoAdvance = false
        onNodeWithText("open a").performClick()
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        // The screen below has to stay composed while the new one comes in, or the push has nothing to animate.
        onNodeWithText("page a-detail").assertExists()
        onNodeWithText("page a").assertExists()
        mainClock.autoAdvance = true
        waitForIdle()
        onNodeWithText("page a-detail").assertExists()
        onNodeWithText("page a").assertDoesNotExist()
    }

    @Test
    fun modalBottomSheetHidesTheTabBar() = runComposeUiTest {
        setContent {
            val nav = rememberKnitNavigator(ScaffoldTab.entries, ScaffoldConfig)
            KnitScaffold(
                navigator = nav,
                tabs = {
                    tab(ScaffoldTab.A, "Tab A", scaffoldIcon)
                    tab(ScaffoldTab.B, "Tab B", scaffoldIcon)
                },
                entryProvider = entryProvider {
                    entry<Page> {
                        var sheet by remember { mutableStateOf(false) }
                        if (sheet) {
                            KnitModalBottomSheet(onDismissRequest = { sheet = false }) { Text("sheet content") }
                        }
                        Button(onClick = { sheet = true }) { Text("show sheet") }
                    }
                },
            )
        }
        onNodeWithText("Tab A").assertExists()
        onNodeWithText("show sheet").performClick()
        waitForIdle()
        onNodeWithText("sheet content").assertExists()
        onNodeWithText("Tab A").assertDoesNotExist()
    }
}
