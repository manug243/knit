package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Serializable
private data object RegisteredHome : NavKey

@Serializable
private data class RegisteredDetail(val id: Int) : NavKey

@Serializable
private data object NotRegistered : NavKey

class KnitRoutesTest {
    @Test
    fun registeredRoutesHaveASerializer() {
        val module = knitRoutes {
            route<RegisteredHome>()
            route<RegisteredDetail>()
        }.serializersModule

        assertNotNull(module.getPolymorphic(NavKey::class, RegisteredHome))
        assertNotNull(module.getPolymorphic(NavKey::class, RegisteredDetail(1)))
    }

    @Test
    fun unregisteredRouteHasNoSerializer() {
        val module = knitRoutes { route<RegisteredHome>() }.serializersModule

        assertNull(module.getPolymorphic(NavKey::class, NotRegistered))
    }
}
