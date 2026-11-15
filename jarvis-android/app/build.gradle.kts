plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "br.com.rivaldo.jarvisai"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.rivaldo.jarvisai"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "0.6.0"
        buildConfigField("String", "PICOVOICE_ACCESS_KEY", "\"${System.getenv("PICOVOICE_ACCESS_KEY") ?: ""}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("ai.picovoice:porcupine-android:4.0.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
