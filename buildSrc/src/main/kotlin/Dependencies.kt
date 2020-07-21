object Versions {
    const val kotlin = "1.3.72"
    const val junit = "4.12"
    const val stately = "1.0.2"
    const val coroutines = "1.3.5-native-mt"
    const val koin = "2.1.5"
    const val serialization = "0.20.0"
    const val ktor = "1.3.2"
    const val logback = "1.2.1"
    const val retrofit = "2.7.1"
    const val glide = "4.9.0"
    const val material = "1.2.0-alpha06"
    const val moshi = "1.9.2"
    const val klaster = "0.3.4"
    const val likeButton = "0.2.3"
    const val once = "1.2.2"
    const val flick = "1.7.0"
    const val materialDialogs = "3.1.1"
    const val dragDropper = "0.3.1"
    const val recyclerViewFastScroller = "0.1.3"
    const val spotlight = "2.0.1"
    const val modernAndroidPreferences = "-SNAPSHOT"
    const val kotpref = "2.10.0"
    const val subsamplingCore = "3.10.0"
    const val subsamplingPdf = "0.1.0"

    object AndroidX {
        const val appCompat = "1.1.0"
        const val coreKTX = "1.3.0-rc01"
        const val fragment = "1.2.4"
        const val constraintLayout = "2.0.0-beta5"
        const val cardView = "1.0.0"
        const val recyclerView = "1.1.0"
        const val viewPager = "1.0.0"
        const val legacy = "1.0.0"
        const val lifecycle = "2.2.0"
        const val room = "2.2.5"
        const val test = "1.2.0"
        const val espresso = "3.2.0"
    }
}

object Deps {
    const val junit = "junit:junit:${Versions.junit}"
    const val stately = "co.touchlab:stately-common:${Versions.stately}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    const val klaster = "com.github.rongi:klaster:${Versions.klaster}"
    const val likeButton = "com.github.jd-alexander:LikeButton:${Versions.likeButton}"
    const val once = "com.jonathanfinerty.once:once:${Versions.once}"
    const val flick = "me.saket:flick:${Versions.flick}"
    const val materialDialogs = "com.afollestad.material-dialogs:core:${Versions.materialDialogs}"
    const val dragDropper = "com.github.andb3:DragDropper:${Versions.dragDropper}"
    const val recyclerViewFastScroller =
        "com.quiph.ui:recyclerviewfastscroller:${Versions.recyclerViewFastScroller}"
    const val spotlight = "com.github.takusemba:spotlight:${Versions.spotlight}"
    const val modernAndroidPreferences =
        "com.github.Maxr1998:ModernAndroidPreferences:${Versions.modernAndroidPreferences}"
    const val kotpref = "com.chibatching.kotpref:kotpref:${Versions.kotpref}"


    object KotlinTest {
        const val common = "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlin}"
        const val annotations =
            "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlin}"
        const val jvm = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"
        const val junit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    }

    object Coroutines {
        const val common =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Versions.coroutines}"
        const val jdk = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val native =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}"
        const val android =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }

    object Ktor {
        const val server = "io.ktor:ktor-server-netty:${Versions.ktor}"
        const val serverTest = "io.ktor:ktor-server-tests:${Versions.ktor}"
        const val client = "io.ktor:ktor-client-core:${Versions.ktor}"
        const val clientJvm = "io.ktor:ktor-client-apache:${Versions.ktor}"
    }

    object Koin {
        const val core = "org.koin:koin-core:${Versions.koin}"
        const val android = "org.koin:koin-android:${Versions.koin}"
        const val androidScope = "org.koin:koin-android-scope:${Versions.koin}"
        const val androidViewModel = "org.koin:koin-android-viewmodel:${Versions.koin}"
    }

    object Retrofit {
        const val core = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    }

    object Glide {
        const val core = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val okhttp3 = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
        const val compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val coreKTX = "androidx.core:core-ktx:${Versions.AndroidX.coreKTX}"
        const val fragment = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}"
        const val cardView = "androidx.cardview:cardview:${Versions.AndroidX.cardView}"
        const val recyclerView =
            "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerView}"
        const val viewPager = "androidx.viewpager:viewpager:${Versions.AndroidX.viewPager}"
        const val legacy = "androidx.legacy:legacy-support-v4:${Versions.AndroidX.legacy}"
        const val test = "androidx.test:runner:${Versions.AndroidX.test}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.AndroidX.espresso}"

        object Lifecycle {
            const val liveDataCore =
                "androidx.lifecycle:lifecycle-livedata-core-ktx:${Versions.AndroidX.lifecycle}"
            const val liveData =
                "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}"
            const val viewModel =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        }

        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.AndroidX.room}"
            const val compiler = "androidx.room:room-compiler:${Versions.AndroidX.room}"
            const val testing = "androidx.room:room-testing:${Versions.AndroidX.room}"
            const val ktx = "androidx.room:room-ktx:${Versions.AndroidX.room}"
        }

    }

    object SubsamplingImageView {
        const val core =
            "com.davemorrissey.labs:subsampling-scale-image-view:${Versions.subsamplingCore}"
        const val pdf = "de.number42:subsampling-pdf-decoder:${Versions.subsamplingPdf}@aar"
    }
}
