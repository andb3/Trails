package com.andb.apps.trails.ui.settings.preferences

import android.graphics.Typeface
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andb.apps.trails.util.dpToPx
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

open class TitlePreference(key: String) : Preference(key){
    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        holder.title.apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 36f
            layoutParams = (layoutParams as ConstraintLayout.LayoutParams).also {
                it.marginStart = dpToPx(8)
                it.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            }
            visibility = View.VISIBLE
        }
        holder.iconFrame.visibility = View.GONE
    }
}

// Preference DSL functions
inline fun PreferenceScreen.Builder.title(key: String, block: TitlePreference.() -> Unit) {
    addPreferenceItem(TitlePreference(key).apply(block))
}