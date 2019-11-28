package com.andb.apps.trails.ui.settings.preferences

import android.content.Intent
import android.net.Uri
import com.andb.apps.trails.R
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter

class LicensePreference(key: String) : Preference(key) {

    var link: String? = null

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        iconRes = R.drawable.ic_book_black_24dp
        super.bindViews(holder)
        holder.itemView.setOnClickListener {
            if (link != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.context.startActivity(intent)
            }

        }
    }

}

// Preference DSL functions
inline fun PreferenceScreen.Builder.license(key: String, block: LicensePreference.() -> Unit) {
    addPreferenceItem(LicensePreference(key).apply(block))
}