plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("org.jetbrains.dokka")
}

tasks.dokkaHtml {
    moduleName.set("Stream Measurement App")
    dokkaSourceSets {
        create("main") {
            sourceRoots.from(file("src/main/java"))
            includes.from(rootProject.file("MODULE.md"))
            reportUndocumented.set(false)

            perPackageOption { matchingRegex.set(".*\\.data\\.device");        suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.data\\.stream_segment"); suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.data\\.velocity_point"); suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.data\\.user");           suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.ui\\.theme");            suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.ui\\.screens");          suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.ui\\.components.*");     suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.ui\\.utils");            suppress.set(true) }
            perPackageOption { matchingRegex.set(".*\\.ui");                    suppress.set(true) }
            perPackageOption { matchingRegex.set("cz\\.cvut\\.fel\\.android_app"); suppress.set(true) }
        }
    }
}

android {
    namespace = "cz.cvut.fel.android_app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "cz.cvut.fel.android_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.location)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}