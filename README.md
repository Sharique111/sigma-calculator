# Sigma Calculator

A scientific calculator for Android: trig (degrees or radians), logs, powers, roots,
factorials, calculation history, keyboard support, and light/dark mode. Works fully offline.

## Install on your phone

1. On your Android phone, open this repository's **Releases** page (right side of the repo page).
2. Under the newest release, tap **Sigma-Calculator.apk** to download it.
3. Open the downloaded file and tap **Install**. If asked, allow your browser or Files app to
   install unknown apps.

Newer versions install over older ones and keep your history.

## How it's built

Every change pushed to `main` triggers the **Build Android APK** workflow (see the Actions tab),
which builds a signed APK and publishes it as a new release.

| Path | What it is |
| --- | --- |
| `app/src/main/assets/www/` | The calculator itself (HTML, CSS, JavaScript, fonts) |
| `app/src/main/java/.../MainActivity.java` | The Android screen that shows the calculator |
| `app/src/main/res/` | App icon, name and theme colors |
| `keystore/sigma.keystore` | The key that signs the APK, so updates install over earlier versions |
| `.github/workflows/build-apk.yml` | The automatic build |

The signing key's password is in `app/build.gradle`. That's fine for a personal app you install
yourself. Before publishing on Google Play, create a new private key and keep it out of the repository.
