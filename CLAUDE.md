# knit

Compose-Multiplatform-Library, mit der Entwickler auf iOS und Android mit wenig Code native
OS-Navigation bekommen, aufgebaut auf Navigation 3. iOS bekommt eine echte Liquid-Glass-Bottom-Navigation
und möglichst auch eine native Back-/Top-Navigation. Nutzer der Library schreiben kein Swift.

Phasen, Befunde, Risiken, offene Punkte und Status stehen in [docs/PLAN.md](docs/PLAN.md).

## Regeln

- Antworten auf Deutsch. Code, Kommentare und Commits auf Englisch.
- Nicht direkt auf `main` arbeiten. Ablauf wie in compose-skeleton: Feature-Branch, PR nach `develop`, PR nach `main`.
- Pushen und PRs nur nach Rückfrage bei Manuel.
- `docs/PLAN.md` nach jeder abgeschlossenen Phase aktualisieren.
- Nach jeder Phase kurz berichten: was läuft, was ungeprüft ist.
- `https://github.com/AMiketta/cmp-ios-chrome` gehört einem Kollegen und ist strikt read-only: keine Pushes,
  Issues, PRs oder Kommentare. Ideen übernehmen ist erlaubt. Der Code steht unter MIT, bei wörtlicher Übernahme
  nennenswerter Teile den Lizenzhinweis erhalten.
- `compose-skeleton` und WeList nur lesen, dort nichts ändern.
- Nichts als erledigt melden, was nicht wirklich gebaut oder getestet wurde.
- iOS kann unter Windows nicht gebaut werden, und Manuel hat keinen Mac. iOS wird nur über die macOS-CI und
  Simulator-Screenshots geprüft. Das klar sagen. Das Gefühl von Glass und Swipe-Back muss jemand separat testen.
- Keine Secrets anlegen oder eintragen, das macht Manuel selbst.

## Architektur

- Nav3 ist die einzige Quelle der Wahrheit für die Navigation.
- Ein einziger Compose-Baum. Native Leisten liegen auf iOS als Overlay darüber.
- Kein Swift beim Nutzer, Einstieg bleibt `ComposeUIViewController { ... }`.
- Module: `:knit` (Library), `:example` (Multiplatform-Demo), `iosApp` (Xcode-Hülle für das Beispiel).
- Package `io.github.manug243.knit`, Coordinates `io.github.manug243:knit`.
- Library mit `explicitApi()`.

iOS-Muster: echter `UITabBar` per `UIKitView` mit
`UIKitInteropProperties(interactionMode = NonCooperative, isNativeAccessibilityEnabled = true, placedAsOverlay = true)`.
`UITabBarDelegate` stark halten (`remember`), UITabBar referenziert ihn nur weak. Der Delegate feuert nur bei
Nutzer-Taps, nicht beim Setzen von `selectedItem`.

Ziel-API (Skizze, darf bei guten Gründen abweichen):

```kotlin
val navigator = rememberKnitNavigator(tabs = AppTab.entries, config = savedStateConfig)

KnitScaffold(
    navigator = navigator,
    tabs = { tab(AppTab.Lists, "Listen", KnitIcon(sfSymbol = "list.bullet", android = listIcon)) },
    entryProvider = entryProvider { entry<Route.Lists> { ListsScreen() } },
)
```

## Referenzen

- Aufbau, Code-Stil, CI/CD und README-Design: https://github.com/manug243/compose-skeleton
  (lokal: `C:\Users\manue\Desktop\Projects\Kotlin\compose-skeleton`)
- Nav3-Nutzung: `C:\Users\manue\Desktop\Projects\welist\app\composeApp\src\commonMain\kotlin\de\manuelgilles\welistapp\navigation\`
  (`TabNavigator.kt`, `MainShell.kt`, `Route.kt`)
- Beispiel für die iOS-Brücke: https://github.com/AMiketta/cmp-ios-chrome, Branch `docs/tabbar-overlap` ist der neueste Stand
- Offizieller JetBrains-Ansatz: https://kotlinlang.org/docs/multiplatform/ios-liquid-glass.html und
  `JetBrains/kotlinconf-app`, Vergleich `main...lg-nav`
- Nav3 in CMP: https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html
- Erfahrungsbericht zum UITabBar-Overlay: https://dev.to/shivathapaa/i-built-jetbrains-official-liquid-glass-setup-then-deleted-it-4hoh

## Befehle

Stand nach Phase 0, auf Windows mit `gradlew.bat`:

```bash
./gradlew :knit:desktopTest :knit:testAndroidHostTest
./gradlew :knit:lint
./gradlew :example:assembleDebug
./gradlew :knit:iosSimulatorArm64Test   # nur macOS
```

Die tatsächlich gültigen Task-Namen werden in Phase 0 geprüft und hier korrigiert.
