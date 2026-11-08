package io.github.manug243.knit.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.Knit

private val StripeColors = listOf(
    Color(0xFFE53935),
    Color(0xFFFDD835),
    Color(0xFF43A047),
    Color(0xFF1E88E5),
    Color(0xFF8E24AA),
)
private const val StripeCount = 40

/**
 * Spike: edge-to-edge, high-contrast content that makes it obvious in a screenshot whether a
 * native bar on top really samples the Compose canvas. Replaced by the real demo in phase 1.
 */
@Composable
fun App(bottomBar: @Composable BoxScope.() -> Unit = {}) {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(StripeCount) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(StripeColors[index % StripeColors.size]),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "${Knit.NAME} $index", color = Color.Black)
                    }
                }
            }
            bottomBar()
        }
    }
}
