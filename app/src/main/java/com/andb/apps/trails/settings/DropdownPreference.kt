package com.andb.apps.trails.settings

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.andb.apps.trails.R
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.android.synthetic.main.settings_item_dropdown.view.*

class DropdownPreference(key: String) : Preference(key) {

    var items = Array(0) { "" }
    var initalSelectedPosition: () -> Int = { getInt(0) }
    var onSelect: (position: Int) -> Unit = { position -> commitInt(position) }

    override fun bindViews(holder: PreferencesAdapter.ViewHolder) {
        super.bindViews(holder)
        holder.itemView.settingsDropdownSpinner.apply {
            adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, items)
                .also { it.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item) }
            setSelection(initalSelectedPosition.invoke())
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    Log.d("settingsDropdown", "onItemSelected: $position")
                    onSelect.invoke(position)
                }

            }
        }
    }

    override fun getWidgetLayoutResource() = R.layout.settings_item_dropdown

}

// Preference DSL functions
inline fun PreferenceScreen.Builder.dropdown(key: String, block: DropdownPreference.() -> Unit) {
    addPreferenceItem(DropdownPreference(key).apply(block))
}