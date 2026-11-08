package io.github.manug243.knit.example

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.manug243.knit.KnitTab
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

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

/** Needed so the back stacks can be saved on iOS, where Navigation 3 has no reflection. */
val RouteSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Home::class, Route.Home.serializer())
            subclass(Route.Lists::class, Route.Lists.serializer())
            subclass(Route.Settings::class, Route.Settings.serializer())
            subclass(Route.Detail::class, Route.Detail.serializer())
        }
    }
}
