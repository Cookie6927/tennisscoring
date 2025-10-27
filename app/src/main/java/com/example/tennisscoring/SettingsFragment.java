package com.example.tennisscoring;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("theme")) {
            ThemeUtils.applyTheme(requireContext());
        } else if (key.equals("haptic_feedback")) {
            // The value is already saved, we just need to react to it if necessary
            // In this case, HapticUtils will read the preference directly, so no action is needed here
            // However, it's good practice to acknowledge the change.
            boolean hapticEnabled = sharedPreferences.getBoolean(key, true);
            // You could add a toast or log here if you wanted to confirm the change
        }
    }
}
