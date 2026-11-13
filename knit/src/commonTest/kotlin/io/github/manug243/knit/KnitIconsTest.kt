package io.github.manug243.knit

import kotlin.test.Test
import kotlin.test.assertEquals

class KnitIconsTest {
    @Test
    fun everyIconHasItsOwnSfSymbol() {
        val icons = mapOf(
            "house" to KnitIcons.Home,
            "list.bullet" to KnitIcons.List,
            "magnifyingglass" to KnitIcons.Search,
            "gearshape" to KnitIcons.Settings,
            "plus" to KnitIcons.Add,
            "square.and.arrow.up" to KnitIcons.Share,
            "pencil" to KnitIcons.Edit,
            "trash" to KnitIcons.Delete,
            "heart" to KnitIcons.Favorite,
            "person" to KnitIcons.Person,
            "bell" to KnitIcons.Notifications,
            "info.circle" to KnitIcons.Info,
            "xmark" to KnitIcons.Close,
            "checkmark" to KnitIcons.Check,
            "ellipsis" to KnitIcons.More,
        )
        icons.forEach { (symbol, icon) -> assertEquals(symbol, icon.sfSymbol) }
    }
}
