plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

import java.util.Properties

val isGithubActions = System.getenv("GITHUB_ACTIONS") == "true"

val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingProp(key: String): String? =
    if (isGithubActions) System.getenv(key) else keystoreProperties.getProperty(key)

android {
    namespace = "com.example.friendly_words"
    compileSdk = 35

    defaultConfig {
        applicationId = "pg.autyzm.friendlywords"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val path = signingProp("KEYSTORE_PATH")
            storeFile = path?.let { file(it) }
            storePassword = signingProp("KEYSTORE_PASSWORD")
            keyAlias = signingProp("KEY_ALIAS")
            keyPassword = signingProp("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    val room_version = "2.7.1"
    val nav_version = "2.9.0"
    implementation(project(":shared"))
    implementation ("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("io.coil-kt:coil-compose:2.3.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("com.composables:core:1.32.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

}