plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.comfyuiwebsocket"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.comfyuiwebsocket"
        minSdk = 31
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("io.ktor:ktor-client-android:1.6.4")
    implementation("io.ktor:ktor-client-websockets:1.6.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}