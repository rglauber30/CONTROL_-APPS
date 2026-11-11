plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.controlplus"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.controlplus"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = "4.0." + (System.getenv("VERSION_CODE") ?: "1")
    }

    // Mesma chave sempre: permite instalar versões novas por cima, sem desinstalar
    signingConfigs {
        create("controlplus") {
            storeFile = file("controlplus.jks")
            storePassword = "controlplus2026"
            keyAlias = "controlplus"
            keyPassword = "controlplus2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("controlplus")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.webkit:webkit:1.11.0")
}
