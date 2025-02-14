plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)


}

android {
    namespace = "com.chenhao.youbike"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chenhao.youbike"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.0.2"

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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    val room_version = "2.6.1"

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //gson
    implementation ("com.google.code.gson:gson:2.11.0")

    //okhttp3
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //google map
    implementation("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")

    //Room Libs
    implementation ("androidx.room:room-runtime:$room_version")
    implementation ("androidx.room:room-common:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version")


}