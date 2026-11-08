package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

/**
 * Builds the [SavedStateConfiguration] that [rememberKnitNavigator] needs. Navigation 3 saves its
 * back stacks with polymorphic serialization, which has no reflection on iOS, so every [NavKey]
 * subclass has to be registered. Each one must be `@Serializable`.
 *
 * ```
 * val RouteConfig = knitRoutes {
 *     route<Route.Home>()
 *     route<Route.Detail>()
 * }
 * ```
 *
 * A route that is missing here crashes when it is saved, so register all of them.
 */
public fun knitRoutes(block: KnitRoutes.() -> Unit): SavedStateConfiguration {
    val routes = KnitRoutes().apply(block)
    return SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                for (registration in routes.registrations) registration()
            }
        }
    }
}

/** Collects the routes of [knitRoutes]. */
public class KnitRoutes @PublishedApi internal constructor() {
    @PublishedApi
    internal val registrations: MutableList<PolymorphicModuleBuilder<NavKey>.() -> Unit> = mutableListOf()

    /** Registers [T], which has to be `@Serializable`. */
    public inline fun <reified T : NavKey> route() {
        registrations += { subclass(T::class, serializer<T>()) }
    }
}
