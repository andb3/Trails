package com.andb.apps.trails.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.andb.apps.trails.BuildConfig
import com.andb.apps.trails.R
import com.andb.apps.trails.TestActivity
import com.andb.apps.trails.data.local.*
import com.andb.apps.trails.ui.settings.preferences.dialog
import com.andb.apps.trails.ui.settings.preferences.dropdown
import com.andb.apps.trails.ui.settings.preferences.license
import com.andb.apps.trails.ui.settings.preferences.title
import de.Maxr1998.modernpreferences.helpers.*

object SettingsLayout {
    fun create(context: Context) = screen(context){

        preferenceFileName = KEY_SHAREDPREFS_NAME

        title("settings_title"){
            titleRes = R.string.settings_title
        }

        dropdown(KEY_STARTING_REGION){
            titleRes = R.string.settings_starting_region
            iconRes = R.drawable.ic_terrain_black_24dp
            items = context.resources.getStringArray(R.array.base_regions)
            initalSelectedPosition = {getInt(1)-1}
            onSelect = { position->
                commitInt(position+1)
                Log.d("settingsDropdown", "commit ${position + 1}, now ${getInt(-1)}")
            }
        }

        dropdown(KEY_SORT_REGIONS){
            titleRes = R.string.settings_sort_regions
            iconRes = R.drawable.ic_sort_black_24dp
            items = context.resources.getStringArray(R.array.settings_sort_options)
            initalSelectedPosition = { getInt(1) }
        }

        dropdown(KEY_SORT_AREAS){
            titleRes = R.string.settings_sort_areas
            iconRes = R.drawable.ic_sort_black_24dp
            items = context.resources.getStringArray(R.array.settings_sort_options)
            initalSelectedPosition = { getInt(0) }
        }


        dialog(KEY_NIGHT_MODE){
            titleRes = R.string.settings_theme_night_mode_title
            iconRes = R.drawable.ic_weather_night
            contents {
                title(res = titleRes)
                val items = context.resources.getStringArray(R.array.settings_theme_night_mode_options).toMutableList()
                items.removeAt(if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.P) 3 else 2)
                val initialSelection = when(getInt(Prefs.nightMode)){
                    AppCompatDelegate.MODE_NIGHT_NO->0
                    AppCompatDelegate.MODE_NIGHT_YES->1
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY->2
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM->2
                    AppCompatDelegate.MODE_NIGHT_AUTO_TIME->3
                    else->0 //never should be called
                }

                listItemsSingleChoice(
                    items = items,
                    initialSelection = initialSelection,
                    waitForPositiveButton = true
                ) { dialog, index, text ->
                    when(index){
                        0->commitInt(AppCompatDelegate.MODE_NIGHT_NO)
                        1->commitInt(AppCompatDelegate.MODE_NIGHT_YES)
                        2->commitInt(if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        3->commitInt(AppCompatDelegate.MODE_NIGHT_AUTO_TIME)
                    }
                    Prefs.nightMode = getInt(Prefs.nightMode)
                    AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)
                }

                cornerRadius(literalDp = 8f)

            }
        }

        subScreen {
            titleRes = R.string.settings_about
            iconRes = R.drawable.ic_info_outline_black_24dp

            title("about_title"){
                titleRes = R.string.settings_about
            }

            pref("about_version") {
                titleRes = R.string.settings_about_version
                iconRes = R.drawable.ic_info_outline_black_24dp
                summary = BuildConfig.VERSION_NAME
            }

            categoryHeader("about_licenses"){
                titleRes = R.string.settings_about_licenses
            }

            license("pref_moshi") {
                title = "Moshi"
                summary = "Apache License 2.0"
            }

        }

        pref("powered_by"){
            titleRes = R.string.settings_powered_by
            iconRes = R.drawable.ic_language_black_24dp
            onClicked {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://SkiMap.org"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return@onClicked true
            }
        }

        if (BuildConfig.DEBUG){
            pref("test_activity"){
                title = "Test"
                iconRes = R.drawable.ic_settings_black_24dp
                onClicked {
                    val intent = Intent(context, TestActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return@onClicked true
                }
            }
        }
    }
}