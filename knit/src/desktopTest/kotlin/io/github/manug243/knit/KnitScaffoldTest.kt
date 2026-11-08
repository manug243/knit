package io.github.manug243.knit

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
