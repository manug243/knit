package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Serializable
private sealed interface SealedRoute : NavKey {
    @Serializable
    data object Home : SealedRoute

    @Serializable
    data class Detail(val id: Int, val name: String) : SealedRoute
}

@Serializable
private data object Stranger : NavKey

/**
 * Saves and restores keys the way Navigation 3 does. Lives in the desktop tests because the Android
 * host tests have no real `Bundle` to write to.
 */
class KnitSealedRoutesTest {
    private val keys = ListSerializer(PolymorphicSerializer(NavKey::class))

    @Test
    fun sealedRoutesSurviveSavingWithoutRegistration() {
        val config = knitRoutes<SealedRoute>()
        val stack: List<NavKey> = listOf(SealedRoute.Home, SealedRoute.Detail(7, "seven"))

        val restored = decodeFromSavedState(keys, encodeToSavedState(keys, stack, config), config)

        assertEquals(stack, restored)
    }

    @Test
    fun sealedRoutesRejectAKeyOutsideTheHierarchy() {
        val config = knitRoutes<SealedRoute>()

        assertFailsWith<SerializationException> {
            encodeToSavedState(keys, listOf<NavKey>(Stranger), config)
        }
    }
}
