package net.psimarron.bitme;

import android.app.Activity;
import android.os.Bundle;

// Die gesamte Seite für die Einstellungen.
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Unsere eigene Konfigurationselemente eintragen
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
