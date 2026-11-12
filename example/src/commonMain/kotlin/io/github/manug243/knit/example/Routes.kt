package io.github.manug243.knit.example

import androidx.navigation3.runtime.NavKey
import io.github.manug243.knit.KnitTab
import io.github.manug243.knit.knitRoutes
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object Lists : Route

    @Serializable
    data object Settings : Route

    /** A screen below a tab, opened by tapping a stripe. The bar is hidden here. */
    @Serializable
    data class Detail(val tab: String, val index: Int) : Route
}

enum class AppTab(override val root: NavKey) : KnitTab {
    Home(Route.Home),
    Lists(Route.Lists),
    Settings(Route.Settings),
}

/** Lets the back stacks be saved. Covers every subclass of [Route], nothing to register per screen. */
val RouteSavedStateConfiguration = knitRoutes<Route>()
