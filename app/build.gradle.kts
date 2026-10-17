plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Each GitHub build gets a higher version number, so new APKs install over old ones.
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

// Google Play builds are signed with the private upload key, which GitHub passes in from its
// encrypted secrets. It is never stored in this repository. Run with -PplayUpload to use it.
val uploadKeystore: String? = System.getenv("UPLOAD_KEYSTORE_FILE")
val signForPlay = project.hasProperty("playUpload") && uploadKeystore != null

android {
    namespace = "app.sigma.calculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.sigma.calculator"
        minSdk = 24          // Android 7.0 and newer
        targetSdk = 36     // Android 16, required by Google Play
        versionCode = buildNumber
        versionName = "2.0.$buildNumber"
    }

    signingConfigs {
        create("sigma") {
            storeFile = file("../keystore/sigma.keystore")
            storePassword = "sigmacalc"
            keyAlias = "sigma"
            keyPassword = "sigmacalc"
        }
        if (uploadKeystore != null) {
            create("upload") {
                storeFile = file(uploadKeystore)
                storePassword = System.getenv("UPLOAD_KEYSTORE_PASSWORD")
                keyAlias = "upload"
                keyPassword = System.getenv("UPLOAD_KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Shrink the app: removes unused code from libraries (about 4x smaller download).
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // The APK you install yourself uses the "sigma" key; the Google Play bundle uses "upload".
            signingConfig = signingConfigs.getByName(if (signForPlay) "upload" else "sigma")
        }
        debug {
            signingConfig = signingConfigs.getByName("sigma")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // Jetpack Compose: Android's toolkit for building screens in Kotlin.
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Unit tests for the calculator engine.
    testImplementation("junit:junit:4.13.2")
}
