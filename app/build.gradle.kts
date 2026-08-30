import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// Single source of truth for deployment config is the gitignored .env at repo root.
val dotEnv = Properties().apply {
    val f = rootProject.file(".env")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "app.wejustmet"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "app.wejustmet"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        val convexUrl = dotEnv.getProperty("CONVEX_URL") ?: System.getenv("CONVEX_URL") ?: ""
        buildConfigField("String", "CONVEX_URL", "\"$convexUrl\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(variantOf(libs.convex.mobile) { artifactType("aar") }) {
        isTransitive = true
    }
    implementation(libs.kotlinx.serialization.json)
}
