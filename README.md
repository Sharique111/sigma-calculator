# Sigma Calculator

A scientific calculator for Android, written in Kotlin with Jetpack Compose.
Trig in degrees or radians, logs, powers, roots, factorials, percent, calculation history,
physical keyboard support, and light/dark mode. Works fully offline.

## Install on your phone

1. On your Android phone, open this repository's **Releases** page (right side of the repo page).
2. Under the newest release, tap **Sigma-Calculator.apk** to download it.
3. Open the downloaded file and tap **Install**. If asked, allow your browser or Files app to
   install unknown apps.

Newer versions install over older ones.

## How the code is organized

All the Kotlin code is in `app/src/main/java/app/sigma/calculator/`.

| File | What it does | Edit it to… |
| --- | --- | --- |
| `MainActivity.kt` | Where the app starts. Picks light or dark and shows the screen. | Change start-up behaviour |
| `CalculatorViewModel.kt` | The calculator's brain: what's typed, what each key does, history. | Change how keys behave |
| `Keys.kt` | The list of keys on the keypad, row by row. | Add, remove or move keys |
| `SettingsStore.kt` | Saves history and settings on the phone. | Save something new |
| `engine/Evaluator.kt` | Works out the maths in a typed calculation. | Add a new function (e.g. asin) |
| `engine/NumberFormat.kt` | Formats results: commas, decimals, 1.5e20. | Change how numbers look |
| `ui/CalculatorScreen.kt` | Draws the screen: top bar, display, keypad, history. | Change layout and sizes |
| `ui/theme/Theme.kt` | All colors and fonts. | Change the look |

Other parts:

| Path | What it is |
| --- | --- |
| `app/src/test/.../EvaluatorTest.kt` | Tests that check the maths. They run before every build. |
| `app/src/main/res/` | App icon, app name, fonts, and the start-up background color |
| `app/build.gradle.kts` | Build settings: app id, version, libraries |
| `keystore/sigma.keystore` | The key that signs the APK, so updates install over earlier versions |
| `.github/workflows/build-apk.yml` | The automatic build on GitHub |

### How a key press flows through the app

1. You tap **7**. `CalcKey` in `CalculatorScreen.kt` calls `vm.press("7")`.
2. `press()` in `CalculatorViewModel.kt` adds "7" to `expression`.
3. `expression` is Compose state, so the screen redraws itself with the new text.
4. The live preview calls `Evaluator(...).evaluate(expression)` and shows "= …".
5. On **=**, the result is formatted, shown, and added to history, which `SettingsStore` saves.

## How it's built

Every change pushed to `main` triggers the **Build Android APK** workflow (see the Actions tab):
it runs the maths tests, builds a signed APK, and publishes it as a new release.

To work on it yourself, install [Android Studio](https://developer.android.com/studio), choose
**File → Open**, select this folder, and press **Run** to start it on an emulator or your phone.

The signing key's password is in `app/build.gradle.kts`. That's fine for a personal app you install
yourself. Before publishing on Google Play, create a new private key and keep it out of the repository.

The earlier web-based version of the app is still available as release v1.0.2.
