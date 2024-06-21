plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.objectbox") // Apply last.
}


android {
    namespace = "com.jacksonke.teresapassword"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jacksonke.teresapassword"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }



    buildTypes {
        // https://developer.android.com/build/build-variants
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    // Encapsulates your external native build configurations.
    externalNativeBuild {

        // Encapsulates your CMake build configurations.
        cmake {

            version = "3.22.1"

            // Provides a relative path to your CMake build script.
            path = file("CMakeLists.txt")
        }
    }

    ndkVersion = "26.1.10909125"
}

dependencies {

    implementation(project(":patternlockview"))

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}