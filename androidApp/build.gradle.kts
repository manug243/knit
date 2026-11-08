// AGP 9 no longer allows com.android.application together with the Kotlin Multiplatform plugin,
// so the Android launcher is a thin module on top of the shared :example module.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.manug243.knit.example"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.manug243.knit.example"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":example"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.mp.runtime)
}
