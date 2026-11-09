package io.github.manug243.knit.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import io.github.manug243.knit.KnitIcon

private fun vectorIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black)).build()

val HomeIcon = KnitIcon(
    sfSymbol = "house",
    vector = vectorIcon("home", "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"),
)

val ListsIcon = KnitIcon(
    sfSymbol = "list.bullet",
    vector = vectorIcon(
        "lists",
        "M3,13h2v-2H3v2zM3,17h2v-2H3v2zM3,9h2V7H3v2zM7,13h14v-2H7v2zM7,17h14v-2H7v2zM7,7v2h14V7H7z",
    ),
)

/** Action of the detail screens: opens the next one. */
val NextIcon = KnitIcon(
    sfSymbol = "plus",
    vector = vectorIcon("next", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"),
)

val SettingsIcon = KnitIcon(
    sfSymbol = "gearshape",
    vector = vectorIcon(
        "settings",
        "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z",
    ),
)
