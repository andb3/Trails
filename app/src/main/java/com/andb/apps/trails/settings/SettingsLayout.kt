package com.andb.apps.trails.settings

import android.content.Context
import com.andb.apps.trails.R
import de.Maxr1998.modernpreferences.helpers.screen

object SettingsLayout {
    fun create(context: Context) = screen(context){
        title("settings_title"){
            titleRes = R.string.settings_title
        }
    }
}