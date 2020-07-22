plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(29)
    signingConfigs {
        getByName("debug") {
            storeFile = file("../../signing/debug.jks")
            storePassword = "3fJpumZX4vP7q4"
            keyPassword = "yDGe5HJyLr2eTF"
            keyAlias = "trails_debug"
        }
    }
    defaultConfig {
        applicationId = "com.andb.apps.trails"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 4
        versionName = "0.8.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments.put("room.schemaLocation", "$projectDir/schemas")
            }
        }
        resValue("string", "app_name", "Trails")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = true
            //shrinkResources true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Trails Beta")
        }
    }

    androidExtensions {
        isExperimental = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mutableMapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Deps.kotlin)
    implementation(Deps.Coroutines.jdk)
    implementation(Deps.Coroutines.android)
    implementation(Deps.AndroidX.appCompat)
    implementation(Deps.AndroidX.coreKTX)
    implementation(Deps.AndroidX.fragment)
    implementation(Deps.AndroidX.constraintLayout)
    implementation(Deps.AndroidX.cardView)
    implementation(Deps.AndroidX.recyclerView)
    implementation(Deps.AndroidX.viewPager)
    implementation(Deps.AndroidX.legacy)
    implementation(Deps.AndroidX.Lifecycle.liveData)
    implementation(Deps.AndroidX.Lifecycle.liveDataCore)
    implementation(Deps.material)

    testImplementation(Deps.junit)
    androidTestImplementation(Deps.AndroidX.test)
    androidTestImplementation(Deps.AndroidX.espresso)

    /*database libraries*/
    implementation(Deps.AndroidX.Room.runtime) //sql database
    kapt(Deps.AndroidX.Room.compiler) //sql database
    testImplementation(Deps.AndroidX.Room.testing) // Test helpers
    implementation(Deps.AndroidX.Room.ktx) //coroutines in room

    implementation(Deps.Retrofit.core) //networking
    implementation(Deps.Retrofit.moshiConverter) //networking convert from json
    implementation(Deps.moshi)

    implementation(Deps.Glide.core) //image loader & cache
    implementation(Deps.Glide.okhttp3)
    kapt(Deps.Glide.compiler) //image loader & cache

    implementation(Deps.Koin.android) //dependency injection
    implementation(Deps.Koin.androidScope)
    implementation(Deps.Koin.androidViewModel)

    implementation(Deps.klaster) //inline recyclerview adapters
    implementation(Deps.likeButton) //favorites button
    implementation(Deps.once) //one-off operations
    implementation(Deps.flick) //flick dismiss images
    implementation(Deps.materialDialogs) //dialog library
    implementation(Deps.dragDropper) //rv drag and drop
    implementation(Deps.recyclerViewFastScroller)
    implementation(Deps.spotlight) //onboarding
    implementation(Deps.modernAndroidPreferences) //settings
    implementation(Deps.kotpref) //preferences
    implementation(Deps.SubsamplingImageView.core) //unified imageview
    implementation(Deps.SubsamplingImageView.pdf) //pdf plugin for above
}
