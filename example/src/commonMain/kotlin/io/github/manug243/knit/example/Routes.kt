package io.github.manug243.knit.example

import androidx.navigation3.runtime.NavKey
import io.github.manug243.knit.KnitTab
import io.github.manug243.knit.knitRoutes
import kotlinx.serialization.Serializable

/** Every screen of the demo. Arguments are ids, the data itself lives in [PantryStore]. */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Lists : Route

    @Serializable
    data object Inbox : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Settings : Route

    /** Second level of the Lists tab: the items of one list. */
    @Serializable
    data class ListDetail(val listId: Int) : Route

    /** Third level of the Lists tab. Has no knit top bar on purpose, see [ItemDetailScreen]. */
    @Serializable
    data class ItemDetail(val itemId: Int) : Route

    /** Second level of the Settings tab. */
    @Serializable
    data object About : Route
}

enum class AppTab(override val root: NavKey) : KnitTab {
    Lists(Route.Lists),
    Inbox(Route.Inbox),
    Search(Route.Search),
    Settings(Route.Settings),
}

/** Lets the back stacks be saved. Covers every subclass of [Route], nothing to register per screen. */
val RouteSavedStateConfiguration = knitRoutes<Route>()
