package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlin.reflect.KClass
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
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

/**
 * Builds the [SavedStateConfiguration] for a sealed route hierarchy in one line. Every subclass of
 * [R] is covered, also the ones added later, so no route can be forgotten.
 *
 * ```
 * @Serializable
 * sealed interface Route : NavKey {
 *     @Serializable data object Home : Route
 *     @Serializable data class Detail(val id: Int) : Route
 * }
 *
 * val RouteConfig = knitRoutes<Route>()
 * ```
 *
 * [R] has to be a `@Serializable` sealed class or interface, and all keys on the back stacks have to
 * belong to it. Use the builder overload for routes that do not share a sealed parent.
 */
public inline fun <reified R : NavKey> knitRoutes(): SavedStateConfiguration =
    sealedRoutesConfiguration(R::class, serializer<R>())

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal fun <R : NavKey> sealedRoutesConfiguration(type: KClass<R>, serializer: KSerializer<R>): SavedStateConfiguration {
    require(serializer.descriptor.kind == PolymorphicKind.SEALED) {
        "knitRoutes<${type.simpleName}>() needs a @Serializable sealed class or interface. " +
            "Register other routes with knitRoutes { route<...>() }."
    }
    return SavedStateConfiguration {
        serializersModule = SerializersModule {
            // The sealed serializer knows all of its subclasses, so it stands in for every key.
            polymorphicDefaultSerializer(NavKey::class) { key ->
                @Suppress("UNCHECKED_CAST")
                if (type.isInstance(key)) serializer as SerializationStrategy<NavKey> else null
            }
            polymorphicDefaultDeserializer(NavKey::class) { serializer }
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
