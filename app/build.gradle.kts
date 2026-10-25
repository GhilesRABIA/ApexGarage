plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Le numéro de version monte à chaque fabrication sur GitHub (voir .github/workflows/apk.yml).
// En local (Android Studio), il vaut 1 par défaut.
val buildNumber = (project.findProperty("buildNumber") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "com.apexgarage.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.apexgarage.app"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.$buildNumber"
    }

    // Clé de signature fixe : indispensable pour installer chaque nouvelle version
    // PAR-DESSUS l'ancienne sans perdre tes données. Garde ce dépôt privé et ne perds pas ce fichier.
    signingConfigs {
        create("apex") {
            storeFile = rootProject.file("keystore/apexgarage.jks")
            storePassword = "apexgarage"
            keyAlias = "apexgarage"
            keyPassword = "apexgarage"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("apex")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
