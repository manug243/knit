pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Must differ from the :knit subproject. Apps that include knit as a composite build with
// TYPESAFE_PROJECT_ACCESSORS would otherwise get two getKnit() accessors and fail to configure.
rootProject.name = "knit-root"
include(":knit")
include(":example")
include(":androidApp")
