package com.andb.apps.trails.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import de.Maxr1998.modernpreferences.PreferencesAdapter

class SettingsViewModel(context: Context) : ViewModel() {
    val preferencesAdapter by lazy { PreferencesAdapter(SettingsLayout.create(context)) }
}