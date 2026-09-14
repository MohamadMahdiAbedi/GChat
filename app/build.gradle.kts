plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    //alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ir.gchat"
    compileSdk = 37

    defaultConfig {
        applicationId = "ir.gchat"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
            isCoreLibraryDesugaringEnabled = true
        }
    buildFeatures {
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.window.size.class1)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.om252345:composemeshgradient:0.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(libs.androidx.appcompat)
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    implementation("io.github.huarangmeng:latex-base:1.5.4-kt2.1.0")
    implementation("io.github.huarangmeng:latex-parser:1.5.4-kt2.1.0")
    implementation("io.github.huarangmeng:latex-renderer:1.5.4-kt2.1.0")
    //implementation("com.airbnb.android:lottie-compose:6.5.2")

    // RaTeX-CMP
    //implementation("io.github.darriousliu:ratex-android:0.1.14")
    implementation("com.ibm.icu:icu4j:78.3")
}