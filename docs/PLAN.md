# knit: Plan

Stand: 2026-11-12

## Status

| Phase | Inhalt | Status |
|---|---|---|
| 0 | Grundgerüst | gebaut und getestet, iOS über die macOS-CI (Xcode 26) grün |
| 1 | Tabs | gebaut und getestet: Navigator, Scaffold, Glass-`UITabBar`. Auf dem iPhone (iOS 26.2, AltStore) von Manuel angesehen: Glass, Tab-Wechsel, Detail, Swipe-Back laufen. Android: Pixel 10 (Android 17) per adb geprüft |
| 2 | Back und Top-Bar | weitgehend fertig und auf `develop`: Navigationsleiste mit Zurück, Titel, Actions, Titel aus dem Screen, Tab-Crossfade, Sheet- und Snackbar-Hilfen, iOS-typische Animation, `NavigationBackHandler`. Ungeprüft: Wischen vom Rand auf Android (Predictive-Back-Animation), Crossfade und Sheets auf einem Gerät, iOS-Build nach PR #10 |
| 3 | Veröffentlichung | in Vorbereitung: Lückenliste gegen WeList steht unten, danach `wasmJs`-Target, API-Check, README, Release |
| 4 | `UINavigationController`-Backend (experimentell) | offen |

## Test auf dem iPhone (ohne Mac)

Die CI baut im iOS-Job zusätzlich eine unsignierte IPA (Artefakt `knit-unsigned-ipa`, Release-Build für Geräte). Der Job
läuft bei PRs von `develop` nach `main` und per manuellem Start (`workflow_dispatch`). Installation mit AltStore unter
Windows und einer eigenen Test-Apple-ID: IPA laden und entpacken, aufs iPhone bringen, in AltStore öffnen. Die Signatur
hält 7 Tage, AltStore erneuert sie, solange AltServer läuft und PC und iPhone im selben WLAN sind. Für die Glass-Leiste
braucht das iPhone iOS 26, ab iOS 18.2 läuft die Demo mit der klassischen Leiste. Ungeprüft: der IPA-Schritt selbst und
die Installation, beides wurde noch nie ausgeführt.

## Ergebnis Glass-Spike (2026-11-08)

Der Spike (`example/src/iosMain/.../SpikeTabBar.kt`) kompiliert unter Xcode 26 und läuft im Simulator (CI-Lauf von
PR 3, iOS-Job grün). Der Screenshot zeigt einen schwebenden Kapsel-Tabbar mit hervorgehobenem Auswahl-Pill, durch den
der Compose-Inhalt unscharf durchscheint (Text eines Streifens hinter der Leiste). Das ist das Liquid-Glass-Aussehen,
und es entsteht mit einem Stock-`UITabBar` ohne eigene Appearance-Konfiguration über einem einzigen Compose-Baum.

Nicht belegt, weil nur ein statischer Screenshot vorliegt: Verhalten beim Scrollen (Brechung, Minimieren), Tippen und
Wechseln der Tabs, Rotation, iPad, Tastatur. Die feste Höhe von 49 pt plus Inset passt im Screenshot, wird aber in
Phase 1 zur Laufzeit gemessen. Das Gefühl muss weiterhin jemand auf einem Gerät testen.

## Stand Phase 1 (2026-11-08)

Gebaut: `KnitNavigator` (ein Back-Stack pro Tab, WeList-Verhalten), `KnitScaffold`, `KnitIcon`, Tab-DSL,
`LocalKnitContentPadding`. Plattformhülle per `expect`: Material 3 mit Bar und Rail ab 600 dp für Android und Desktop
(`MaterialShell`), `UITabBar` als Overlay für iOS. Die Bar ist nur an der Tab-Wurzel sichtbar und blendet bei Tastatur aus.
Das Beispiel nutzt den Scaffold, der Spike ist entfernt.

Befund zum Verdacht aus dem Auftrag: In Nav3 1.1.2 gibt es `rememberDecoratedNavEntries(backStack, entryDecorators,
entryProvider)` und `NavDisplay(entries = ...)`. Der Scaffold hält die Entries aller Tabs dekoriert und zeigt nur die des
aktiven Tabs. Ob dadurch auch ViewModels inaktiver Tabs überleben, ist nicht getestet (nur `rememberSaveable`-Zustand).

Geprüft unter Windows: 12 Navigator-Tests, 2 Tab-DSL-Tests, 5 Compose-UI-Tests auf Desktop (Tab-Wechsel, Zustand und
Detail-Stack inaktiver Tabs, Bar ausgeblendet unter der Wurzel), Lint, Android-Build.

Geprüft über die macOS-CI (PR 5, Xcode 26, grün): `KnitPlatformShell.ios.kt` kompiliert, `iosSimulatorArm64Test` und
`xcodebuild` laufen. Der Start-Screenshot zeigt die Glass-Leiste über den Streifen, der Inhalt scheint unscharf durch.

Ungeprüft:
- iOS-Verhalten: Die Höhe ist `max(49 pt + Inset, sizeThatFits)`, ob das auf iOS 26 stimmt, ist offen, ebenso das Padding
  am Listenende. Auch Tippen, Tab-Wechsel, Ein- und Ausblenden, Tastatur und Tab-Items zur Laufzeit sind nur
  geschrieben, es gibt nur einen Screenshot vom Start. Dafür dient der Test auf dem iPhone (siehe unten).
- Die Material-Bar und die Rail auf einem Android-Gerät. Nur der Desktop-Pfad lief in Tests.
- ViewModel-Erhalt inaktiver Tabs, Rotation, Prozess-Tod.

Bekannte Lücken und Folgearbeiten:
- `BackHandler` ist in dieser CMP-Version veraltet, Ersatz ist `NavigationEventHandler`. Migration offen.
- Ein Tab-Wechsel blendet seit Phase 2 über (vorher keine Animation).
- `NavigationBar` und `NavigationRail` hängen an `material3 1.12.0-alpha03`, einer Vorabversion.
- Gleiche `NavKey`s in zwei Tabs teilen sich den ViewModel-Speicher (Content-Key), das ist nicht abgesichert.
- Snackbars und Sheets, die unter der nativen Leiste landen, haben noch keine Lösung in der API.

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

Stand Phase 2 (2026-11-08): Gebaut sind `KnitScaffold(topBar = { key -> KnitTopBar(title) })` (Titel pro `NavKey`, Zurück-Button
unterhalb des Tab-Starts, ruft `navigator.back()`), die Material-Leiste und auf iOS ein `UINavigationBar` als Overlay.
Ohne `UINavigationController` kennt der Bar den Statusbereich nicht und legt seinen Inhalt am Rand der eigenen Frame ab.
Der erste Entwurf (Frame bis zum Bildschirmrand) setzte den Button in die Statusleiste, wie der CI-Screenshot zeigte.
Jetzt ist die Frame genau 44 dp hoch und sitzt unter der Statusleiste. `LocalKnitContentPadding` hat einen Top-Wert (ohne
Statusleiste). Dazu `knitRoutes { route<T>() }` für die `SavedStateConfiguration`. Swipe-Back läuft über die
Compose-Geste, von Manuel auf dem iPhone bestätigt. Die CI startet die App zusätzlich direkt auf der Detailansicht
(`-knit-detail`) und legt `detail.png` ab.

Danach gebaut (Branch `feature/chrome-extras`, lokal auf Desktop und Android-Host getestet, iOS noch nicht gebaut):
- `KnitTopBar(title, actions)` mit `KnitAction(label, icon, onClick)`. iOS: `UIBarButtonItem` mit `UIAction` rechts in der
  Leiste, Android/Desktop: `IconButton`s in der `TopAppBar`. Die erste Action liegt außen.
- `KnitTopBarOverride(KnitTopBar(...))` im Screen setzt die Leiste aus dem Screen heraus (Titel aus geladenen Daten,
  Actions je nach Zustand). Gewinnt gegen den `topBar`-Parameter, gespeichert pro `NavKey`, bis der Key aus allen Stacks
  verschwindet. Dafür umhüllt das Scaffold den `entryProvider`. Zwei gleiche Keys in einem Stack teilen sich die Leiste.
- Tab-Wechsel blendet per `Crossfade` (180 ms) über, kein Push/Pop.
- `KnitTabBarHidden()` hält die Tab-Bar verborgen, solange es in der Komposition ist, gedacht neben Sheets und Dialogen.
  `Modifier.knitContentPadding()` für Dinge am Bildschirmrand wie den `SnackbarHost`. Beides ist nur eine Hilfe, kein
  automatisches Verhalten: Ein `ModalBottomSheet` ohne `KnitTabBarHidden()` liegt auf iOS weiter unter der nativen Leiste.

Auf dem iPhone (iOS 26.2) von Manuel bestätigt: Die Navigationsleiste mit Zurück-Button, Titel und Action sitzt. Beim
Öffnen einer Detailseite fahren Tab-Bar (nach unten) und Navigationsleiste (von oben) animiert aus, beim Verlassen,
auch per Swipe-Back, wieder ein. Ungeklärt: ob die Leisten dabei dem Finger folgen wie bei einem
`UINavigationController`. Die Animation hängt am Navigator-Zustand und nicht an der Geste, das spricht eher dagegen. Ein erster Fehler zeigte sich nur auf dem Gerät: Die
`update`-Funktion des `UIKitView` las ein Nicht-State-Feld und lief bei einem Screen, der nach dem ersten Frame geöffnet
wurde, nicht mehr, die Leiste blieb leer. Der CI-Screenshot übersah das, weil er die Detailseite direkt beim Start öffnet.
Fix: die Funktion fängt `topBar` ein und läuft damit bei jeder Änderung.

Animation der nativen Leisten (Branch `feature/native-bar-animation`, iOS ungebaut und ungesehen): Das Wegfahren nach
oben und unten war nicht iOS-typisch. Jetzt bleibt die Navigationsleiste fest unter der Statusleiste, ihre Items wechseln
mit `setItems(animated = true)` und die Leiste blendet nur aus, wenn ein Screen keine hat. Die Tab-Bar blendet aus statt
nach unten zu fahren, beide schalten die Touches im ausgeblendeten Zustand ab. Offen: ob `setItems(animated)` auf dem
Gerät wie ein Push und Pop wirkt und ob die Tab-Bar zeitlich zum Seitenwechsel passt.

Back-Handling (Branch `feature/back-handling`, lokal auf Desktop und Android-Host gebaut und getestet, Gerät und iOS
offen): Der veraltete `BackHandler` (ui-backhandler) ist durch `NavigationBackHandler` aus `navigationevent-compose` 1.1.0
ersetzt, die Abhängigkeit war schon transitiv auf dem iOS-Klassenpfad. In der Android-Demo ist
`android:enableOnBackInvokedCallback="true"` gesetzt, damit die System-Animation für Predictive Back greift. `NavDisplay`
animiert das Zurück auf einer Detailseite selbst. Der Wechsel von einem Tab-Start zurück zum Home-Tab läuft über den
Handler und überblendet erst beim Abschluss der Geste, ohne Fortschrittsanimation. Ungeprüft: Zurück auf einem
Android-Gerät mit Android 14 oder neuer, und der iOS-Build mit der neuen Abhängigkeit.

Der veraltete `BackHandler` ist ersetzt (PR #10). Auf dem Pixel 10 (Android 17) per adb geprüft: Tab-Wechsel, Zurück vom
Tab-Start zu Home, Detailseite mit Top-Bar, Zurück von der Detailseite. Ungeprüft: Wischen vom Rand (Predictive-Back-
Animation), Crossfade zwischen Tabs und Sheets mit `KnitTabBarHidden()` auf einem Gerät.

**Phase 3, Veröffentlichung:** WeList als Probelauf vor dem Release (die API wird mit 0.1.0 zugesagt), dann API-Check
(`binary-compatibility-validator`), README im Skeleton-Stil mit Bildern, Release-Pipeline angleichen (`macos-26` wie die
CI), Versionsname klären (`0.1.0-alpha01` wegen Material 3 Alpha), Release.

### Lückenliste knit gegen WeList (2026-11-09, nur gelesen)

Geprüft wurden `navigation/` (`TabNavigator`, `MainShell`, `Route`, `TabBar`, `TopLevelDestination`), die Build-Dateien und
die Screens auf Overlays und Insets. WeList wird nicht geändert, der Probelauf läuft in einer temporären Kopie außerhalb
des Repos.

Größere Lücken, die die API vor 0.1.0 betreffen:
1. **Web-Target fehlt.** WeList baut für Android, iOS, Desktop und `wasmJs` (im Kern auch `js`). knit hat Android, iOS und
   Desktop. Ohne `wasmJs`-Variante bricht der Web-Build von WeList an knit ab. Lösung: `wasmJs`-Target mit der
   Material-Leiste, ohne native Leisten. Ob Navigation 3 und `lifecycle-viewmodel-navigation3` dort laufen, ist ungeprüft.
2. **Versionen.** WeList: Kotlin 2.4.10, Compose 1.11.1, Material 3 1.10.0-alpha05. knit: Kotlin 2.3.21, Compose 1.12.2,
   Material 3 1.12.0-alpha03, Navigation 3 1.1.2. Ein neuerer Compiler liest ältere Bibliotheken, aber Gradle hebt WeLists
   Compose-Version auf die von knit an. Ob das ohne Konflikte geht, zeigt nur ein Versuch.
3. **Eigenes Design.** WeList nutzt kein `MaterialTheme`, sondern `WeListTheme`, eigene Icons (`IconSpec`) und eine
   selbst gebaute Tab-Bar und Rail. Auf iOS ersetzt die native Leiste das, gewollt. Auf Android, Desktop und Web sähe die
   Material-Leiste von knit in Standardfarben anders aus. Es braucht Theming oder einen Slot für eine eigene Leiste.

Kleinere Punkte:
4. Tab-Titel sind `StringResource`s. Der `tabs`-Block war nicht `@Composable`, dort ging `stringResource` nicht.
   **Erledigt (Branch `feature/composable-tabs`):** `tabs` ist jetzt `@Composable KnitTabsScope<T>.() -> Unit`.
5. WeLists Icons (`IconSpec`) sind keine `ImageVector`s, sondern SVG-Pfade auf einem 24er-Raster, die per `Canvas` als
   Linien gezeichnet werden (`WeListIcon(icon, tint)`). Ein `ImageVector` zu verlangen, hätte eine Umwandlung erzwungen.
   **Erledigt:** `KnitIcon(sfSymbol) { tint -> ... }` nimmt auch ein beliebiges Composable (zusätzlich zu
   `KnitIcon(sfSymbol, vector)`). Eigene Leisten zeichnen es mit `KnitIcon.Content(tint)`. Für WeList also
   `KnitIcon("list.bullet") { tint -> WeListIcon(tab.icon, tint = tint) }`.
6. `ListsScreen` (Tab-Start) und `StoreListScreen` haben eigene Snackbar-Hosts, dazu ein globaler in `UIEventHandler`.
   Auf iOS müssten sie `Modifier.knitContentPadding()` nutzen. Sheets wurden dort nicht gefunden.
7. Die Screens zeichnen ihre Header und Zurück-Buttons selbst (`LargeHeader`, `onBack`). Ohne `topBar` gibt es auf iOS
   nur den Swipe-Back und keinen nativen Zurück-Button. Ob WeList die native Leiste übernimmt, ist eine Gestaltungsfrage.
8. Die Screens müssen auf iOS `LocalKnitContentPadding` auf ihre scrollbaren Inhalte anwenden.

Schon abgedeckt: ein Backstack pro Tab, erneutes Tippen zurück zum Start, Zurück vom Tab-Start zum ersten Tab, Leiste nur
auf dem Start, Rail ab 600 dp, ViewModel-Store und gespeicherter Zustand pro Eintrag, polymorphe Route-Konfiguration,
`select(Lists)` aus einem tiefen Screen, hochgehobener Zustand wie `partyNotice` (der `entryProvider` ist ein Parameter).
Unterschiede im Verhalten: knit blendet die Leiste bei offener Tastatur aus und überblendet Tabs.

**Phase 4, experimentell:** `UINavigationController`-Backend, nur falls Phase 2 sich nicht gut genug anfühlt.

## Theming auf Android, Desktop und Web (2026-11-09)

Entscheidung: iOS bleibt immer die native Leiste mit dem Look des Systems. Auf Android, Desktop und Web soll die Anwendung
ihr eigenes Design anwenden können, der Material-Standard ist nur der Ausgangspunkt. Dafür hat `KnitScaffold` drei
Parameter, die iOS ignoriert:
- `colors: KnitColors` (Standard `KnitDefaults.colors()`, folgt dem umgebenden `MaterialTheme`, einzeln überschreibbar) für
  Tab-Bar, Rail und Top-Bar.
- `tabBar: @Composable (KnitTabBarState) -> Unit` ersetzt die Material-Tab-Bar. Der State liefert die Tabs mit Titel, Icon,
  Auswahl und `onClick` und sagt, ob eine Leiste unten oder eine Rail (ab 600 dp) gezeichnet wird. Die Navigationsleisten-
  Insets behandelt die eigene Leiste selbst.
- `topBarContent: @Composable (KnitTopBarState) -> Unit` ersetzt die Material-Top-Bar. Der State liefert Titel, Actions,
  ob ein Zurück-Button gezeigt wird und `onBack`. Die Statusleisten-Insets behandelt die eigene Leiste selbst.

Dazu ein `wasmJs`-Target, damit Apps mit Web-Ziel (WeList) knit einbinden können. Es nutzt die Material-Leisten, der
Web-Test ist nur ein Kompilieren (`:knit:compileKotlinWasmJs`), die CI führt es aus. Ungeprüft: Verhalten im Browser,
iOS-Build mit den geänderten Signaturen.

## Weniger Aufwand für Nutzer (2026-11-12, Branch `feature/less-boilerplate`)

Ziel: mit möglichst wenig Code auf beiden Plattformen das native Aussehen und Verhalten. Die Befunde aus dem
WeList-Probelauf (Insets und Abstände von Hand, toter Zurück-Button) waren der Maßstab.

- **`knitRoutes<Route>()`**: eine Zeile für eine `@Serializable` sealed Hierarchie statt einer `route<T>()`-Zeile pro
  Screen. Keine Route kann mehr vergessen werden. Technisch steht der Serializer des sealed Typs als Standard-Serializer
  für `NavKey` im Modul. Die Builder-Variante bleibt für Routen ohne gemeinsamen sealed Typ.
- **`knitSafePadding()` und `Modifier.knitSafeArea()`**: ein Wert für alles, was ein Screen freihalten muss
  (Systemleisten, Display-Cutout, knits Leisten). Vorher rechnete jeder Screen `safeDrawing` und
  `LocalKnitContentPadding` selbst zusammen, und das Beispiel zählte dabei auf Android die Statusleiste unter der
  Top-Bar doppelt. Die Tastatur ist bewusst nicht enthalten, `knitSafeArea()` markiert die Insets als verbraucht, damit
  ein `imePadding()` weiter innen nicht doppelt zählt. `LocalKnitContentPadding` bleibt für Sonderfälle.
- **Versteckte iOS-Leisten geben ihre Fläche frei**: Die `UIKitView`s von Navigations- und Tab-Leiste haben Höhe 0,
  solange die Leiste ausgeblendet ist. Das ist der Versuch einer Lösung für den toten Zurück-Button aus dem
  WeList-Probelauf. Die Annahme dahinter (die Hüllen-View des Interop schluckt Touches, auch wenn die Leiste selbst
  keine annimmt) ist nicht belegt.
- README mit Schnellstart, Example auf die neuen Helfer umgestellt.

Geprüft unter Windows: `:knit:desktopTest` (35 Tests, davon neu: Sichern und Wiederherstellen eines Stacks mit
`knitRoutes<Route>()`, Ablehnung fremder Keys, Safe-Padding im Scaffold), `:knit:testAndroidHostTest`,
`:knit:compileKotlinWasmJs`, `:knit:lint`, `:androidApp:lintDebug`, `:androidApp:assembleDebug`.

Ungeprüft:
- Alles auf iOS: Der geänderte `KnitPlatformShell.ios.kt` wurde nicht einmal kompiliert (kein Mac, der iOS-Job läuft
  erst beim PR nach `main`). Offen ist, ob Höhe 0 den toten Zurück-Button behebt, ob die Leisten beim Wiedereinblenden
  sauber erscheinen und ob die Werte von `knitSafePadding()` auf dem iPhone stimmen.
- `knitSafePadding()` auf einem Android-Gerät. Im Desktop-Test gibt es keine System-Insets, dort ist der Wert immer 0.
- Sichern und Wiederherstellen mit `knitRoutes<Route>()` lief nur auf dem Desktop. Die Android-Host-Tests haben kein
  echtes `Bundle`, ein Prozess-Tod auf einem Gerät wurde nicht getestet.

Bewusst nicht gemacht: Large Titles, Minimieren der Tab-Leiste beim Scrollen und ein `UINavigationController`-Backend.
Das lässt sich ohne Gerät nicht beurteilen.

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
