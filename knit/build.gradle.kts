plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "io.github.manug243"
version = "0.1.0"

kotlin {
    explicitApi()

    android {
        namespace = "io.github.manug243.knit"
        compileSdk = 37
        minSdk = 24

        withHostTestBuilder {}
    }
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.mp.runtime)
            api(libs.compose.mp.ui)
            api(libs.navigation3.ui)
            api(libs.savedstate)
            implementation(libs.compose.mp.animation)
            implementation(libs.navigationevent.compose)
            implementation(libs.compose.mp.foundation)
            implementation(libs.compose.mp.material3)
            implementation(libs.lifecycle.viewmodel.navigation3)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("desktopTest").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.mp.ui.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    // Signing is only enforced when signing credentials are provided (release workflow).
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates("io.github.manug243", "knit", version.toString())

    pom {
        name.set("knit")
        description.set("Native iOS and Android navigation chrome for Compose Multiplatform, built on Navigation 3.")
        url.set("https://github.com/manug243/knit")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("manug243")
                name.set("manug243")
                url.set("https://github.com/manug243")
            }
        }
        scm {
            url.set("https://github.com/manug243/knit")
            connection.set("scm:git:https://github.com/manug243/knit.git")
            developerConnection.set("scm:git:ssh://git@github.com/manug243/knit.git")
        }
    }
}
