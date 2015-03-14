package net.psimarron.bitme;

import android.os.Bundle;
import android.preference.PreferenceFragment;

// Pflegt die Einstellungen der Anwendung.
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Konfiguration der Einstellungen laden
        addPreferencesFromResource(R.xml.preferences);
    }
}
