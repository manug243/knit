package io.github.manug243.knit

import androidx.navigation3.runtime.NavKey

/**
 * One top-level destination of the app. Usually implemented by an enum, one entry per tab.
 * The first tab of the list passed to [rememberKnitNavigator] is the home tab.
 */
public interface KnitTab {
    /** Start screen of the tab. It is the bottom of the tab's back stack and is never popped. */
    public val root: NavKey
}
