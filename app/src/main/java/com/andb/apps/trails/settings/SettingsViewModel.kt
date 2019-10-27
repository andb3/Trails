package com.andb.apps.trails.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import de.Maxr1998.modernpreferences.PreferencesAdapter

class SettingsViewModel(context: Context) : ViewModel() {
    val preferencesAdapter by lazy { PreferencesAdapter(SettingsLayout.create(context)) }
}