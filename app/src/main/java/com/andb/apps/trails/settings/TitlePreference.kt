package com.andb.apps.trails.settings

import android.graphics.Typeface
import com.afollestad.materialdialogs.MaterialDialog
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

open class TitlePreference(key: String) : Preference(key){
    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        holder.title.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 36f
        }
    }
}

// Preference DSL functions
inline fun PreferenceScreen.Builder.title(key: String, block: TitlePreference.() -> Unit) {
    addPreferenceItem(TitlePreference(key).apply(block))
}