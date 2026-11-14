# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
While the version is `0.x`, minor releases may contain breaking changes.

## [Unreleased]

Planned for the first release, 0.1.0. Nothing is published yet.

### Added

- `KnitScaffold` and `rememberKnitNavigator`: tab navigation on Navigation 3 with one back stack per tab.
- iOS: a native `UITabBar` and `UINavigationBar` as overlays above one Compose tree, Liquid Glass with the iOS 26 SDK.
- Android, Desktop and Web: Material 3 tab bar, a rail from 600 dp, a top app bar and predictive back.
- Tab bars and the keyboard: the Material bar stays under the open keyboard, the native iOS bar fades out but keeps its area clear, so the content of a tab start screen does not move when the keyboard opens or closes.
- `KnitTopBar`, `KnitAction` and `KnitTopBarOverride` for titles and actions per screen.
- `knitRoutes<Route>()` to save a sealed route hierarchy without registering each route.
- `knitSafePadding()` and `Modifier.knitSafeArea()` for the space the system bars and knit's bars cover.
- `KnitIcons` with common icons for both platforms, tab badges, `KnitTabReselectEffect`, `KnitModalBottomSheet`, `KnitTabBarHidden` and `navigator.navigate(tab, key)`.
- Theming of the Material bars with `KnitDefaults.colors()`, an accent color for the native iOS bars, and slots for own tab and top bars.
- Targets: Android (`minSdk 24`), Desktop (JVM), iOS (`iosArm64`, `iosSimulatorArm64`) and Web (`js`, `wasmJs`).

[Unreleased]: https://github.com/manug243/knit/commits/develop
