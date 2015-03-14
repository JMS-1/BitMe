package net.psimarron.bitme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

// Pflegt die Einstellungen der Anwendung.
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguration der Einstellungen laden
        addPreferencesFromResource(R.xml.preferences);

        // Änderungen aktualisieren die Anzeige
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        // Beschreibungen setzen
        updateSummaries();
    }

    // Aktualisiert alle Beschreibungen.
    public void updateSummaries() {
        // Beschreibungen setzen
        ListPreference level = (ListPreference) findPreference(getResources().getString(R.string.pref_strength_key));
        level.setSummary(level.getEntry());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummaries();
    }
}
