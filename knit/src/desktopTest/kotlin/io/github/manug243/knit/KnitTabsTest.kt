package io.github.manug243.knit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private data class Root(val name: String) : NavKey

private enum class Tab(override val root: NavKey) : KnitTab {
    A(Root("a")),
    B(Root("b")),
    C(Root("c")),
}

/** The `tabs` block the way [KnitScaffold] runs it: as a composable that can restart on its own. */
@OptIn(ExperimentalTestApi::class)
class KnitTabsTest {
    private val icon = KnitIcon(
        sfSymbol = "circle",
        vector = ImageVector.Builder(
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).build(),
    )

    @Test
    fun keepsDeclarationOrder() = runComposeUiTest {
        var items = emptyList<KnitTabItem<Tab>>()
        setContent {
            items = composeTabItems {
                tab(Tab.B, "B", icon)
                tab(Tab.A, "A", icon)
            }
        }
        runOnIdle {
            assertEquals(listOf(Tab.B, Tab.A), items.map { it.tab })
            assertEquals(listOf("B", "A"), items.map { it.title })
        }
    }

    @Test
    fun rejectsDuplicateTab() {
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                setContent {
                    composeTabItems<Tab> {
                        tab(Tab.A, "A", icon)
                        tab(Tab.A, "again", icon)
                    }
                }
                waitForIdle()
            }
        }
    }

    @Test
    fun followsRemovedAddedAndReorderedTabs() = runComposeUiTest {
        // Only the block reads this state, so the block restarts on its own, without its caller.
        var declared by mutableStateOf(listOf(Tab.A, Tab.B, Tab.C))
        var items = emptyList<KnitTabItem<Tab>>()
        setContent {
            items = composeTabItems { for (tab in declared) tab(tab, tab.name, icon) }
        }
        runOnIdle { assertEquals(listOf(Tab.A, Tab.B, Tab.C), items.map { it.tab }) }

        declared = listOf(Tab.A, Tab.C)
        runOnIdle { assertEquals(listOf(Tab.A, Tab.C), items.map { it.tab }) }

        declared = listOf(Tab.C, Tab.B, Tab.A)
        runOnIdle { assertEquals(listOf(Tab.C, Tab.B, Tab.A), items.map { it.tab }) }
    }

    @Test
    fun followsABadgeThatOnlyTheBlockReads() = runComposeUiTest {
        var unread by mutableStateOf(2)
        var items = emptyList<KnitTabItem<Tab>>()
        setContent {
            items = composeTabItems {
                tab(Tab.A, "A", icon, badge = unread.takeIf { it > 0 }?.toString())
            }
        }
        runOnIdle { assertEquals("2", items.single().badge) }

        unread = 0
        runOnIdle { assertEquals(null, items.single().badge) }
    }
}
