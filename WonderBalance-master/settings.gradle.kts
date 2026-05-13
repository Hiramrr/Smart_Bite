// settings.gradle.kts (archivo raíz del proyecto)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ← JitPack para MPAndroidChart
    }
}

rootProject.name = "WonderBalance"
include(":app")