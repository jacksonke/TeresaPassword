package com.jacksonke.teresapassword

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_settings)

        val preference = findPreference<Preference>("key_version_name")
        val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        preference!!.summary = packageInfo.versionName
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
