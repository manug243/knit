# knit: Plan

Stand: 2026-11-08

## Status

| Phase | Inhalt | Status |
|---|---|---|
| 0 | Grundgerüst | gebaut und getestet, iOS über die macOS-CI (Xcode 26) grün |
| 1 | Tabs | offen |
| 2 | Back und Top-Bar | offen |
| 3 | Veröffentlichung | offen |
| 4 | `UINavigationController`-Backend (experimentell) | offen |

## Ergebnis Phase 0 (2026-11-08)

Gewählte Versionen, gegen Maven Central geprüft: CMP 1.12.2 (weiterhin letzte stabile, 1.13.0 steht bei beta02), Kotlin
2.3.21, AGP 9.2.1, Gradle 9.4.1, Nav3 UI 1.1.2, lifecycle-viewmodel-navigation3 2.11.0, CMP Material3 1.12.0-alpha03 (für
1.12 gibt es keine stabile Version). Nav3 UI 1.2.0 scheidet aus: Die iOS-klib hat ABI 2.4.0, Kotlin 2.3.21 liest nur bis
2.3.0. Für die Kompatibilität zählt `abi_version` im klib-Manifest, nicht die `kotlin-stdlib`-Version im POM. Ob
`rememberDecoratedNavEntries` in Nav3 1.1.2 verfügbar ist, wird in Phase 1 geprüft.

Abweichung vom Auftrag: Zusätzlich zu `:example` gibt es `:androidApp`, weil AGP 9 `com.android.application` nicht
zusammen mit dem KMP-Plugin erlaubt. `:example` enthält den gemeinsamen Code, `:androidApp` nur die Activity.

Geprüft unter Windows: `:knit:desktopTest`, `:knit:testAndroidHostTest`, `:knit:lint`, `:example:compileKotlinDesktop`,
`:androidApp:lintDebug`, `:androidApp:assembleDebug`.

Geprüft über die macOS-CI (Xcode 26, Commit `cca5f82`): `:knit:iosSimulatorArm64Test`, `xcodebuild` des iOS-Beispiels,
Start im Simulator mit Screenshot. Seitdem läuft der iOS-Job nur noch bei PRs von `develop` nach `main`. Ungeprüft: der
Release-Workflow und die Android-Demo, die gebaut, aber nicht auf einem Gerät oder Emulator gestartet wurde.

## Ziel

Compose-Multiplatform-Library für native OS-Navigation auf iOS und Android, aufgebaut auf Navigation 3.
iOS bekommt eine echte Liquid-Glass-Bottom-Navigation und möglichst eine native Back-/Top-Navigation.
Nutzer der Library schreiben kein Swift.

## Architekturentscheidung

Nav3 bleibt die einzige Quelle der Wahrheit. Ein Compose-Baum. Native Leisten liegen auf iOS als Overlay darüber.
Kein Swift beim Nutzer, Einstieg bleibt `ComposeUIViewController { ... }`.

## Phasen

**Phase 0, Grundgerüst:** Umbau zu `:knit` und `:example` (Multiplatform) plus `iosApp`, Package
`io.github.manug243.knit`, Versionen nach Skeleton (CMP 1.12.2). CI und Release aus Skeleton übernehmen. Zusätzlich
baut die macOS-CI das iOS-Beispiel gegen Xcode 26 und lädt Simulator-Screenshots als Artefakte hoch.

**Phase 1, Tabs:** generischer `KnitNavigator` aus dem WeList-Verhalten mit Unit-Tests, nativer `UITabBar` auf iOS,
Material-3-`NavigationBar` und Rail auf Android, Content-Padding per CompositionLocal, Höhe zur Laufzeit messen,
Tab-Items reaktiv halten, Bar animiert ausblenden (auf Detail-Screens und bei Tastatur). Erste Aufgabe in dieser Phase
ist ein kurzer Spike im `:example`, der belegt, dass Glass über Compose-Inhalt wirklich funktioniert.

**Phase 2, Back und Top-Bar:** `UINavigationBar` als Overlay mit Glass-Back-Button und Actions, iOS-typische Übergänge
und Swipe-Back über den Compose-Back-Gesture (`predictivePopTransitionSpec`). Android: Top-App-Bar und Predictive Back.

**Phase 3, Veröffentlichung:** README im Skeleton-Stil mit GIFs, Release 0.1.0, danach WeList als Praxistest.

**Phase 4, experimentell:** `UINavigationController`-Backend, nur falls Phase 2 sich nicht gut genug anfühlt.

## Befunde (Stand 2026-11-08, aus dem Auftrag)

**knit:** unverändertes KMP-Wizard-Template (`composeApp`, `iosApp`), `main` und `develop` identisch. Versionen dort:
CMP 1.10.3, AGP 8.11.2, Kotlin 2.3.21. Package heute `de.manuelgilles.knit`.

**compose-skeleton als Vorlage:** Module `skeleton` und `example`, `explicitApi()`,
`com.android.kotlin.multiplatform.library`, Targets Android (minSdk 24, compileSdk 37), `jvm("desktop")`, `iosArm64`,
`iosSimulatorArm64`. Versionen: CMP 1.12.2, Kotlin 2.3.21, AGP 9.2.1, vanniktech maven-publish 0.37.0, JDK 21.
Coordinates `io.github.manug243:<name>`. CI: `ubuntu-latest` (Desktop-/Android-Tests, Lint, Example) und `macos-latest`
(`iosSimulatorArm64Test`). Release: Tag `v*` auf macOS, prüft Tag gegen Version, publiziert mit
`publishAndReleaseToMavenCentral`. Secrets im Environment `maven-central`: `MAVEN_CENTRAL_USERNAME`,
`MAVEN_CENTRAL_PASSWORD`, `SIGNING_KEY`, `SIGNING_KEY_ID`, `SIGNING_KEY_PASSWORD`.

**WeList als Vorlage für den Navigator:** Ein `rememberNavBackStack` pro Tab mit `SavedStateConfiguration` (polymorphe
Serialisierung, auf iOS nötig). Verhalten: Tippen auf den aktiven Tab poppt zur Wurzel, Back poppt den Stack, dann zurück
zum ersten Tab, dann `false` (App darf schließen). Bar nur an der Tab-Wurzel, ab 600 dp Rail statt Bottom-Bar. `NavDisplay`
mit `rememberSaveableStateHolderNavEntryDecorator` und `rememberViewModelStoreNavEntryDecorator`. WeList-Versionen:
CMP 1.11.1, Nav3 UI 1.1.1, lifecycle-viewmodel-navigation3 2.10.0, Kotlin 2.4.10.
Verdacht, ungetestet: WeList gibt nur den Stack des aktiven Tabs an `NavDisplay`, dadurch könnten ViewModels inaktiver
Tabs beim Wechsel verloren gehen. Knit soll Entries pro Tab dekoriert halten (prüfen, ob `rememberDecoratedNavEntries`
plus `NavDisplay(entries = ...)` in der gewählten Nav3-Version verfügbar ist).

**cmp-ios-chrome:** Beweist das Muster: echter `UITabBar` per `UIKitView` mit `UIKitInteropProperties(interactionMode =
NonCooperative, isNativeAccessibilityEnabled = true, placedAsOverlay = true)` über einem einzigen Compose-Baum, dazu
`LocalTabBarOverlap`. `UITabBarDelegate` muss stark gehalten werden (`remember`), weil UITabBar ihn nur weak referenziert.
Der Delegate feuert nur bei Nutzer-Taps, nicht beim Setzen von `selectedItem`. Schwächen, die knit nicht übernehmen soll:
keine Nav3-Anbindung, Höhe fest auf 49 pt, Tabs nur einmal in der Factory erfasst, Android-Icons sind der erste Buchstabe,
`onSelectedIndexChange` im Controller-Pfad ignoriert, iOS wird in der CI nicht kompiliert. Bekannte Falle:
`UITabBarController` via `UIKitViewController` in Compose misst oft 0×0, und pro Tab entsteht ein getrennter Compose-Baum.

**JetBrains-Weg:** SwiftUI-Shell mit `TabView` und einem `NavigationStack` pro Tab, ein `ComposeUIViewController` pro
Screen, Compose verwaltet den Stack nicht mehr. Nachteil für eine Library: gespiegelte Navigation in Swift und viel
Swift-Code bei jedem Nutzer. Bewusst nicht als Standard gewählt.

**Plattform:** Letzte stabile CMP-Version war 1.12.2, 1.13.0 ist Beta (vor der Festlegung erneut prüfen). 1.13.0-beta01
bringt `UIKitInteropSafeAreaInsetsPolicy`. Ein Stock-`UITabBar` soll Liquid Glass automatisch übernehmen, wenn die App mit
dem iOS-26-SDK (Xcode 26) gebaut wird und `UIDesignRequiresCompatibility` nicht gesetzt ist. Vergleichbare Library für
Nav3 mit nativem iOS-Chrome wurde nicht gefunden.

## Risiken

- Native Overlays liegen immer über Compose. Snackbars und Sheets im selben Baum können unter der Tabbar landen, dafür
  eine Lösung in der API vorsehen.
- Doppelte Bottom-Insets durch verschachtelte Scaffolds, `imePadding` verhält sich je Plattform anders.
- Compose-Scrolling ist kein `UIScrollView`, Minimize-on-Scroll und Scroll-Edge-Effekte funktionieren daher vermutlich
  nicht (ungeprüft).
- Bare `UITabBar` hat kein `UITab`-API (Such-Tab, iPad-Sidebar).
- Mit CMP 1.12.2 muss WeList (1.11.1) mit hochziehen. Kotlin-Version nicht höher als nötig wählen, Library-klibs dürfen
  Consumer nicht ausschließen.

Machbarkeit laut Einschätzung, ohne Prototyp: Tabs ca. 90 %, native Top-Bar ca. 70 %, iOS-naher Swipe-Back ca. 80 %,
echter `UINavigationController` ca. 45 %, volle Parität ca. 25 %.

## Offene Punkte

- Manuel hat keinen Mac (geklärt am 2026-11-08). iOS-Prüfung läuft nur über die macOS-CI und Simulator-Screenshots.
  Das Gefühl von Glass und Swipe-Back muss jemand separat auf einem echten Gerät oder Mac testen.
- Secrets im Environment `maven-central` trägt Manuel selbst ein.
