package net.psimarron.bitme;

import android.app.Activity;
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
        // Wir sind nicht mehr aktiv
        Activity activity = getActivity();
        if (activity == null)
            return;

        // Neuladen des Spiels erzwingen - im Moment ist das in Ordnung, später wollen wir vielleicht etwas genauer hinschauen
        activity.setResult(Activity.RESULT_OK);

        // Anzeige aktualisieren
        updateSummaries();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Änderungen aktualisieren die Anzeige
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Wird sind nun nicht mehr an Informationen über Änderungen interessiert
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
}
