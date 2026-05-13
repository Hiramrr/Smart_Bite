plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.wonderbalance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wonderbalance"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"https://howupyxjpzofkvxndktl.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"sb_publishable_JxxJbJrWZEiAdDBFkvlrzA_o-weOW2I\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.play.services.auth)
    val version_room = "2.7.0"
    val version_lifecycle = "2.8.7"
    val version_nav = "2.8.9"

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    implementation("androidx.room:room-runtime:$version_room")
    implementation("androidx.room:room-ktx:$version_room")
    ksp("androidx.room:room-compiler:$version_room")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$version_lifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$version_lifecycle")

    implementation("androidx.navigation:navigation-fragment-ktx:$version_nav")
    implementation("androidx.navigation:navigation-ui-ktx:$version_nav")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-core:2.3.11")
    implementation("io.ktor:ktor-client-logging:2.3.11")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}