package net.psimarron.bitme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class AppStarter extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_starter);

        try {
            InputStream reader = getResources().openRawResource(R.raw.intro);
            try {
                ByteArrayOutputStream string = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[10000];

                    for (int n; (n = reader.read(buffer, 0, buffer.length)) > 0; )
                        string.write(buffer, 0, n);

                    TextView intro = (TextView) findViewById(R.id.intro);
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

    public void onStart(View starter) {
        Intent intent = new Intent();
        intent.setClass(this, TheRiddle.class);
        startActivity(intent);
    }
}
