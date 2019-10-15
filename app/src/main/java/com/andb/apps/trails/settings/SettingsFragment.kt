package com.andb.apps.trails.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andb.apps.trails.R
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.android.synthetic.main.settings_layout.*

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private val preferencesAdapter by lazy { PreferencesAdapter(SettingsLayout.create(requireContext())) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_layout, container, false)
        //super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = preferencesAdapter
        }

        Log.d("settingsFragment", "adapter: ${settingsRecycler.adapter}")
    }
}