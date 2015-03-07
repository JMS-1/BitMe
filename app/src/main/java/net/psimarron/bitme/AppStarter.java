package net.psimarron.bitme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// Auf der Startseite wird das Spiel kurz erklärt.
public class AppStarter extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wir müssen sicherstellen, dass der Anwender auch bis zur Schaltfläche navigieren kann
        View view = getLayoutInflater().inflate(R.layout.activity_app_starter, null);

        setContentView(view);

        // Die eigentliche Beschreibung wird aus einer separaten Ressource ausgelesen
        try {
            InputStream reader = getResources().openRawResource(R.raw.intro);
            try {
                ByteArrayOutputStream string = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[10000];

                    for (int n; (n = reader.read(buffer, 0, buffer.length)) > 0; )
                        string.write(buffer, 0, n);

                    TextView intro = (TextView) view.findViewById(R.id.intro);
                    intro.setText(string.toString("UTF-8"));
                } finally {
                    string.close();
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            // Das interessiert und im Moment noch nicht
        }
    }

    // Der Anwender möchte das Spiel nun starten.
    public void onStart(View starter) {
        // Wir erzeugen einen neuen Task und stellen insbesondere sicher, dass die Einführungsseite aus der Historie verschwindet
        Intent intent = new Intent();
        intent.setClass(this, TheRiddle.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
