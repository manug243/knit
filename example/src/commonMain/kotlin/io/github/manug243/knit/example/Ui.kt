package io.github.manug243.knit.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.knitSafeArea
import io.github.manug243.knit.knitSafePadding

/**
 * [knitSafePadding] plus some breathing room, for the `contentPadding` of a list. The list still
 * scrolls behind the bars, the extra space only keeps the first and last row off them.
 */
@Composable
fun listPadding(): PaddingValues {
    val safe = knitSafePadding()
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = safe.calculateStartPadding(direction) + 16.dp,
        top = safe.calculateTopPadding() + 12.dp,
        end = safe.calculateEndPadding(direction) + 16.dp,
        bottom = safe.calculateBottomPadding() + 16.dp,
    )
}

/**
 * Hosts [content] with a snackbar at the bottom. The host sits in `knitSafeArea()`, so the tab bar
 * never covers it.
 */
@Composable
fun SnackbarBox(content: @Composable (SnackbarHostState) -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    Box(Modifier.fillMaxSize()) {
        content(snackbar)
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).knitSafeArea())
    }
}
