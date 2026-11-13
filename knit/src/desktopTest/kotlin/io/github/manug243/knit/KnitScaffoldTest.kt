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
}
