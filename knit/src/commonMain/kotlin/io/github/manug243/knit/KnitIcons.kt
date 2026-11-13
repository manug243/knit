package io.github.manug243.knit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// The vector paths are the Material Design icons (https://fonts.google.com/icons), which are licensed
// under the Apache License 2.0, the same license as this project.

private fun vectorIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black)).build()

/**
 * Ready-made [KnitIcon]s for tabs and actions. Each pairs an SF Symbol for iOS with a Material icon
 * for the other platforms, so common cases need no icon code at all:
 *
 * ```
 * tab(AppTab.Home, "Home", KnitIcons.Home)
 * KnitAction("Share", KnitIcons.Share) { ... }
 * ```
 *
 * The vectors are built on first use, an app only pays for the icons it shows.
 */
public object KnitIcons {
    /** `house` */
    public val Home: KnitIcon by lazy {
        KnitIcon("house", vectorIcon("home", "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"))
    }

    /** `list.bullet` */
    public val List: KnitIcon by lazy {
        KnitIcon(
            "list.bullet",
            vectorIcon("list", "M3,13h2v-2H3v2zM3,17h2v-2H3v2zM3,9h2V7H3v2zM7,13h14v-2H7v2zM7,17h14v-2H7v2zM7,7v2h14V7H7z"),
        )
    }

    /** `magnifyingglass` */
    public val Search: KnitIcon by lazy {
        KnitIcon(
            "magnifyingglass",
            vectorIcon(
                "search",
                "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z",
            ),
        )
    }

    /** `gearshape` */
    public val Settings: KnitIcon by lazy {
        KnitIcon(
            "gearshape",
            vectorIcon(
                "settings",
                "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z",
            ),
        )
    }

    /** `plus` */
    public val Add: KnitIcon by lazy {
        KnitIcon("plus", vectorIcon("add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"))
    }

    /** `square.and.arrow.up` */
    public val Share: KnitIcon by lazy {
        KnitIcon(
            "square.and.arrow.up",
            vectorIcon(
                "share",
                "M16,5l-1.42,1.42l-1.59,-1.59V16h-1.98V4.83L9.42,6.42L8,5l4,-4L16,5zM20,10v11c0,1.1 -0.9,2 -2,2H6c-1.11,0 -2,-0.9 -2,-2V10c0,-1.11 0.89,-2 2,-2h3v2H6v11h12V10h-3V8h3C19.1,8 20,8.89 20,10z",
            ),
        )
    }

    /** `pencil` */
    public val Edit: KnitIcon by lazy {
        KnitIcon(
            "pencil",
            vectorIcon(
                "edit",
                "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z",
            ),
        )
    }

    /** `trash` */
    public val Delete: KnitIcon by lazy {
        KnitIcon("trash", vectorIcon("delete", "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"))
    }

    /** `heart` */
    public val Favorite: KnitIcon by lazy {
        KnitIcon(
            "heart",
            vectorIcon(
                "favorite",
                "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z",
            ),
        )
    }

    /** `person` */
    public val Person: KnitIcon by lazy {
        KnitIcon(
            "person",
            vectorIcon(
                "person",
                "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z",
            ),
        )
    }

    /** `bell` */
    public val Notifications: KnitIcon by lazy {
        KnitIcon(
            "bell",
            vectorIcon(
                "notifications",
                "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.89,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z",
            ),
        )
    }

    /** `info.circle` */
    public val Info: KnitIcon by lazy {
        KnitIcon(
            "info.circle",
            vectorIcon(
                "info",
                "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z",
            ),
        )
    }

    /** `xmark` */
    public val Close: KnitIcon by lazy {
        KnitIcon(
            "xmark",
            vectorIcon(
                "close",
                "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
            ),
        )
    }

    /** `checkmark` */
    public val Check: KnitIcon by lazy {
        KnitIcon("checkmark", vectorIcon("check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"))
    }

    /** `ellipsis` */
    public val More: KnitIcon by lazy {
        KnitIcon(
            "ellipsis",
            vectorIcon(
                "more",
                "M6,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM18,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
            ),
        )
    }
}
