package net.psimarron.bitme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// Auf der Startseite wird das Spiel kurz erklärt.
public class HelpAndIntro extends Activity {
    // Beim Aufruf der Parameter mit der Anzahl der Bits.
    public static final String EXTRA_NUMBER_OF_BITS = "numberOfBits";

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
                    String introText = string.toString("UTF-8");
                    int numberOfBits = getIntent().getIntExtra(EXTRA_NUMBER_OF_BITS, -1);

                    intro.setText(introText.replace("$$MAX$$", Integer.toString((1 << numberOfBits) - 2)));
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
}
