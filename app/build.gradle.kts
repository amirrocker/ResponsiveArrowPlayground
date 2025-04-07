plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("de.mannodermaus.android-junit5") version "1.12.0.0"
}

android {
    namespace = "de.amirrocker.responsivearrowplayground"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.amirrocker.responsivearrowplayground"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)

    implementation(libs.arrow.optics)
    implementation(libs.arrow.optics.compose)
    ksp(libs.arrow.optics.ksp.plugin)

    implementation(libs.kluent.android)
    testImplementation(libs.kluent.android)

    // junit 5
    // JUnit 5 API and Engine
//    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
//
//    // For running JUnit 5 tests on Android (only required if using Robolectric)
//    testImplementation("org.robolectric:robolectric:4.10.3")
//
//    // Enables JUnit 5 Platform support
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.0")

    // (Optional) If you need "Parameterized Tests"
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.0")

    // (Optional) If you also have JUnit 4-based tests
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.12.0")

}