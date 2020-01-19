package com.andb.apps.trails.ui.settings.preferences

import android.content.Intent
import android.net.Uri
import android.util.Log
import com.andb.apps.trails.R
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.onClickView

class LicensePreference(key: String) : Preference(key) {

    var link: String? = null

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        iconRes = R.drawable.ic_book_black_24dp
        super.bindViews(holder)
        onClickView { preference, viewHolder ->
            Log.d("licensePreference", "onClick")
            if (link != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                viewHolder.root.context.startActivity(intent)
                Log.d("licensePreference", "launching browser for $link")
            }
            true
        }
    }

    companion object {
        const val APACHE_2 = "Apache License 2.0"
        const val MIT = "MIT License"
        const val BSD = "BSD License - see source for more info"
    }

}

// Preference DSL functions
inline fun PreferenceScreen.Builder.license(key: String, block: LicensePreference.() -> Unit) {
    addPreferenceItem(LicensePreference(key).apply(block))
}