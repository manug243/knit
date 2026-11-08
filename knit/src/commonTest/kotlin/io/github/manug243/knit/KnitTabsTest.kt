package io.github.manug243.knit

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private data class Root(val name: String) : NavKey

private enum class Tab(override val root: NavKey) : KnitTab {
    A(Root("a")),
    B(Root("b")),
}

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
    fun keepsDeclarationOrder() {
        val items = buildTabItems<Tab> {
            tab(Tab.B, "B", icon)
            tab(Tab.A, "A", icon)
        }
        assertEquals(listOf(Tab.B, Tab.A), items.map { it.tab })
        assertEquals(listOf("B", "A"), items.map { it.title })
    }

    @Test
    fun rejectsDuplicateTab() {
        assertFailsWith<IllegalArgumentException> {
            buildTabItems<Tab> {
                tab(Tab.A, "A", icon)
                tab(Tab.A, "again", icon)
            }
        }
    }
}
