# knit

[![CI](https://github.com/manug243/knit/actions/workflows/ci.yml/badge.svg)](https://github.com/manug243/knit/actions/workflows/ci.yml)

Native iOS and Android navigation chrome for Compose Multiplatform, built on Navigation 3.

One Compose tree, one navigation state, no Swift. iOS gets a real `UITabBar` and `UINavigationBar` (Liquid Glass
when built with the iOS 26 SDK), Android gets Material 3 bars with a rail from 600 dp and predictive back.

## Status

Work in progress, not published yet. The first release will be `0.1.0` on Maven Central as
`io.github.manug243:knit`. Until then the API can still change. See the [changelog](CHANGELOG.md).

The native iOS bars were checked on an iPhone with iOS 26. On Android the tabs, the top bar and back were checked on a
Pixel, the newer additions only in tests. The web target compiles but has not been tried in a browser.

## Compatibility

| knit | Kotlin | Compose Multiplatform | Navigation 3 | Android `minSdk` |
| --- | --- | --- | --- | --- |
| `0.1.0` (unreleased) | 2.4.21 | 1.12.2 | 1.1.2 | 24 |

Targets: Android, Desktop (JVM), iOS (`iosArm64`, `iosSimulatorArm64`) and Web (`js`, `wasmJs`). Liquid Glass needs an app
built with the iOS 26 SDK (Xcode 26).

## Quick start

Routes are Navigation 3 keys. Put them in one sealed hierarchy and knit saves them without any registration:

```kotlin
@Serializable
sealed interface Route : NavKey {
    @Serializable data object Home : Route
    @Serializable data object Settings : Route
    @Serializable data class Detail(val id: Int) : Route
}

enum class AppTab(override val root: NavKey) : KnitTab {
    Home(Route.Home),
    Settings(Route.Settings),
}

val RouteConfig = knitRoutes<Route>()
```

The app is one scaffold. The same code runs on every platform:

```kotlin
@Composable
fun App() {
    val navigator = rememberKnitNavigator(AppTab.entries, RouteConfig)

    KnitScaffold(
        navigator = navigator,
        tabs = {
            tab(AppTab.Home, "Home", KnitIcon(sfSymbol = "house", vector = HomeVector))
            tab(AppTab.Settings, "Settings", KnitIcon(sfSymbol = "gearshape", vector = SettingsVector))
        },
        entryProvider = entryProvider {
            entry<Route.Home> { HomeScreen(onOpen = { navigator.navigate(Route.Detail(it)) }) }
            entry<Route.Settings> { SettingsScreen() }
            entry<Route.Detail> { DetailScreen(it) }
        },
        topBar = { key -> if (key is Route.Detail) KnitTopBar(title = "Item ${key.id}") else null },
    )
}
```

On iOS the entry point stays `ComposeUIViewController { App() }`.

## Screens

The native bars on iOS float above the content, the Material bars on Android sit next to it. A screen does not
have to know: `knitSafePadding()` is everything it has to keep clear, system bars and cutout included.

```kotlin
// Scrolls behind the bars, which is what makes the glass on iOS look right.
LazyColumn(contentPadding = knitSafePadding()) { ... }

// Screens that do not scroll, snackbar hosts, bottom buttons.
Column(Modifier.fillMaxSize().knitSafeArea()) { ... }
```

The keyboard is not part of it. Use `imePadding()` as usual, and do not consume the IME inset around the scaffold.

| Need | Use |
|---|---|
| Title or actions that depend on the screen's state | `KnitTopBarOverride(KnitTopBar(title, actions))` inside the screen |
| A sheet or dialog that the iOS tab bar must not cover | `KnitTabBarHidden()` next to it |
| Routes without a common sealed parent | `knitRoutes { route<A>(); route<B>() }` |
| Titles from resources | the `tabs` block is composable, call `stringResource` in it |

## Icons, badges and more

```kotlin
tab(AppTab.Inbox, "Inbox", KnitIcons.Notifications, badge = if (unread > 0) "$unread" else null)
```

| Need | Use |
|---|---|
| Common icons without icon code (SF Symbol on iOS, Material icon elsewhere) | `KnitIcons.Home`, `List`, `Search`, `Settings`, `Add`, `Share`, `Edit`, `Delete`, `Favorite`, `Person`, `Notifications`, `Info`, `Close`, `Check`, `More` |
| A badge on a tab | `badge = "3"` in `tab(...)`, `null` shows none. A custom `tabBar` gets it as `KnitTabBarItem.badge` |
| Tint of the native bars on iOS | `colors = KnitDefaults.colors(accent = Color(0xFF0A7AFF))`, other platforms ignore it |
| Opaque screens during a push or pop | Every screen gets the theme background automatically. `colors = KnitDefaults.colors(screenBackground = ...)` changes it, `Color.Transparent` switches it off |
| Scroll a list to the top when the current tab is tapped again | `KnitTabReselectEffect { listState.animateScrollToItem(0) }` inside the screen. Only runs on the tab's start screen, deeper screens pop as before |
| A bottom sheet that the iOS tab bar must not cover | `KnitModalBottomSheet(onDismissRequest = { ... }) { ... }` |
| Open a screen inside another tab (deep link, notification) | `navigator.navigate(AppTab.Settings, Route.Account)` |

## Behavior

- One back stack per tab. The stack and the saved state of an inactive tab are kept.
- Tapping the current tab returns to its start screen. On the start screen it runs `KnitTabReselectEffect`.
- Back pops the screen, then leads to the first tab, then leaves the app.
- The tab bar is only shown on a tab's start screen. On iOS it hides while the keyboard is open. The Material bar on
  Android, Desktop and Web stays under the keyboard, so the content does not jump when the keyboard opens or closes.

## Own design on Android, Desktop and Web

iOS always shows the system bars. On the other platforms the Material bars follow the surrounding
`MaterialTheme`. Change single colors with `colors = KnitDefaults.colors(...)` (on iOS only `accent` applies), or draw the bars yourself with the
`tabBar` and `topBarContent` parameters of `KnitScaffold`.

## Android setup

Predictive back needs `android:enableOnBackInvokedCallback="true"` on the `<application>` in the manifest.

## Demo

`:example` is Pantry, a small shopping and pantry list app with in-memory sample data. Every screen shows one
part of knit:

| Screen | Shows |
| --- | --- |
| Lists | `knitSafePadding()` as `contentPadding`, so colored cards scroll behind the bars; `KnitTabReselectEffect` scrolls to the top |
| List detail | `KnitTopBarOverride` with the list's name and two actions (`KnitIcons.Add`, `KnitIcons.Share`) |
| Item detail | No knit top bar on purpose: an own Compose header and back button inside `Modifier.knitSafeArea()` |
| Inbox | `badge` on the tab (unread count, `null` at zero) and a top bar action |
| Search | The tab bar and the keyboard (iOS: the bar leaves, Material: the keyboard covers it), `imePadding()` inside `knitSafeArea()`, `navigate(tab, key)` into List detail |
| Settings | `KnitDefaults.colors(accent = ...)`, `KnitModalBottomSheet`, a snackbar in `knitSafeArea()`, a plain `topBar` title on About |

Run it with `./gradlew :example:run` on the desktop or `./gradlew :androidApp:installDebug` on Android.

## Modules

| Module | Purpose |
| --- | --- |
| `knit` | The library |
| `example` | Shared demo app "Pantry" (Android, Desktop, iOS) |
| `androidApp` | Android launcher for the demo |
| `iosApp` | Xcode project for the demo |

## Build

```shell
./gradlew :knit:desktopTest :knit:testAndroidHostTest :knit:compileKotlinWasmJs :knit:compileKotlinJs
./gradlew :knit:lint :androidApp:lintDebug :androidApp:assembleDebug
```

The project requires JDK 21 and Android SDK platform 37. The iOS tests (`./gradlew :knit:iosSimulatorArm64Test`) and
the iOS demo need macOS with Xcode 26.

## Contributing

Bug reports, ideas and pull requests are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request,
and use [Discussions](https://github.com/manug243/knit/discussions) for questions.

## License

knit is available under the Apache License 2.0. See [LICENSE](LICENSE).
