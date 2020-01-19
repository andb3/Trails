package com.andb.apps.trails.ui.settings

import com.andb.apps.trails.ui.settings.preferences.LicensePreference
import com.andb.apps.trails.ui.settings.preferences.license
import de.Maxr1998.modernpreferences.PreferenceScreen

fun PreferenceScreen.Builder.licenses() {

    license("pref_android_support") {
        title = "AndroidX Support Library"
        summary = LicensePreference.APACHE_2
        link = "https://developer.android.com/jetpack/androidx/versions"
    }

    license("pref_glide") {
        title = "Glide"
        summary = LicensePreference.BSD
        link = "https://github.com/bumptech/glide"
    }

    license("pref_klaster") {
        title = "Klaster"
        summary = LicensePreference.MIT
        link = "https://github.com/rongi/klaster"
    }

    license("pref_koin") {
        title = "Koin"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/InsertKoinIO/koin"
    }

    license("pref_kotpref") {
        title = "Kotpref"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/chibatching/Kotpref"
    }

    license("pref_like_button") {
        title = "Like Button"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/jd-alexander/LikeButton"
    }

    license("pref_lives") {
        title = "Lives"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/adibfara/Lives"
    }

    license("pref_material_dialogs") {
        title = "Material Dialogs"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/afollestad/material-dialogs"
    }

    license("pref_modern_android_prefs") {
        title = "Modern Android Preferences"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/Maxr1998/ModernAndroidPreferences"
    }

    license("pref_moshi") {
        title = "Moshi"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/square/moshi"
    }

    license("pref_once") {
        title = "Once"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/jonfinerty/Once"
    }

    license("pref_rv_fastcroller") {
        title = "RecyclerViewFastScroller"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/quiph/RecyclerView-FastScroller"
    }

    license("pref_subsampling__pdf") {
        title = "Subsampling PDF Decoder"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/num42/subsampling-pdf-decoder"
    }

    license("pref_subsampling_image") {
        title = "Subsampling Scale Image View"
        summary = LicensePreference.APACHE_2
        link = "https://github.com/davemorrissey/subsampling-scale-image-view"
    }
}