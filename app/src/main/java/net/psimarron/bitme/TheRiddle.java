package net.psimarron.bitme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import java.nio.ByteBuffer;

// Das ist die Aktivität mit dem eigentlichen Spiel.
public class TheRiddle extends Activity implements View.OnTouchListener, Animation.AnimationListener, NfcAdapter.CreateNdefMessageCallback {

    // Die Weite der horizontalen Verschiebung des ausgewählten Bits.
    private final static int ANIMATION_OFFSET = 200;

    // Die Zeit in Millisekunden für die horizontale Verschiebung bis zur weitesten Entfernung.
    private final static int ANIMATION_TIME_FLING = 200;

    // Die Startverzögerung in Millisekunden für den Beginn des freien Falls.
    private final static int ANIMATION_TIME_FALL_OFFSET = ANIMATION_TIME_FLING / 4;

    // Die Startverzögerung in Millisekunden zwischen dem Fall der einzelnen Bits.
    private final static int ANIMATION_TIME_FALL_DELAY = ANIMATION_TIME_FALL_OFFSET;

    // Die Fallzeit in Millisekunden für jedes einzelne Bit.
    private final static int ANIMATION_TIME_FALL = 4 * ANIMATION_TIME_FLING - 2 * (ANIMATION_TIME_FALL_OFFSET + ANIMATION_TIME_FALL_DELAY);

    // Das Ergebnis der Änderung der Einstellungen.
    private final static int SETTINGS_RESULT = 1;

    // Das aktuelle Rätsel.
    private Riddle m_currentRiddle;

    // Die Anzeige der minimalen Anzahl von Vertauschungen.
    private TextView m_guess;

    // Die Anzeige der einzelnen Bits.
    private View[] m_bits;

    // Merkt sich den Anfang einer Geste.
    private float m_touchStart;

    // Enthält die Position des aktuell animierten Bits.
    private int m_currentAnimation = -1;

    // Die Anzahl der Bits und damit die Spielstärke.
    private int m_numberOfBits = -1;

    // Hier leben und sterben die kleinen Bits.
    private ViewGroup m_bitContainer;

    // Die Anzahl der Animationen, die noch abgewartet werden müssen.
    private int m_pendingAnimations;

    // Zur Übertragung des Rätsels an ein anderes SmartPhone.
    private NfcAdapter m_nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Die Übertragung an andere Rechner ist optional
        m_nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (m_nfcAdapter != null)
            m_nfcAdapter.setNdefPushMessageCallback(this, this);

        // Voreinstellungen definieren
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Aufbauen
        initialize(savedInstanceState);
    }

    // Erzeugt alle Oberflächenelemente.
    private void initialize(Bundle savedInstanceState) {
        // Neu erzeugen oder rekonstruieren
        m_currentRiddle = new Riddle(getNumberOfBits(), savedInstanceState);
        m_numberOfBits = m_currentRiddle.NumberOfBits;

        setContentView(R.layout.activity_the_riddle);

        // Die relevanten Oberflächenelemente, im Moment verwenden wir eine geschachtelte Darstellung zum Abblenden des Hintergrunds - TODO: vermutlich wäre das eher eine Aufgabe für ein LayerDrawable
        m_bitContainer = (ViewGroup) findViewById(R.id.game_container);
        m_guess = (TextView) findViewById(R.id.view_guess);
        m_bits = new View[m_numberOfBits];

        // Für jedes Bit wird dynamisch eine entsprechende Repräsentation erzeugt
        for (int i = 0; i < m_bits.length; i++) {
            // Am einfachsten geht das aus einer Vorlage
            View bit = getLayoutInflater().inflate(R.layout.bit, m_bitContainer, false);

            // Die Anzeige des Bits vorbereiten
            bit.setOnTouchListener(this);

            // Und dann merken wir uns die Präsentation und schalten die Anzeige frei
            m_bitContainer.addView(m_bits[i] = bit, 0);
        }

        // Initiales zeichnen
        refresh();
    }

    // Ermittelt den Namen für die Einstellung der aktuellen Spielstärke.
    private String getNumberOfBitsSettingName() {
        return getResources().getString(R.string.pref_strength_key);
    }

    // Ermittelt die aktuelle Spielstärke.
    private int getNumberOfBits() {
        String level = PreferenceManager.getDefaultSharedPreferences(this).getString(getNumberOfBitsSettingName(), null);
        if ("A".equals(level))
            return 8;
        else if ("B".equals(level))
            return 9;
        else if ("C".equals(level))
            return 10;
        else
            return 8;
    }

    // Aktualisiert alle Oberflächenelemente.
    private void refresh() {
        // Wir passen auch die Überschrift entsprechend an
        setTitle(getResources().getString(R.string.app_title, m_currentRiddle.Goal));

        // Und den Hinweis auf die minimal benötigte Anzahl von Verschiebungen
        m_guess.setText(Integer.toString(m_currentRiddle.Par));

        // Die Präsentation wird auf Basis der Daten neu angepasst
        for (int index = 0; index < m_bits.length; index++)
            m_bits[index].setActivated(m_currentRiddle.get(index));

        // Dieses Feedback ist vor allem zu Ende des Spiels relevant
        m_guess.setActivated(m_currentRiddle.isMatch());
        m_guess.setSelected(m_currentRiddle.getTries() <= m_currentRiddle.Par);

        // Das Spiel ist zu Ende, wenn die gewünschte Zahl hergestellt wurde
        if (m_currentRiddle.isMatch())
            won();
    }

    // Gewinnen ist langweilig, es kommt einfach nur ein Dialog.
    private void won() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.won)
                .setPositiveButton(R.string.won_close, null)
                .create()
                .show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Solange das Feedback nicht abgeschlossen ist geht hier gar nichts
        if (m_currentAnimation >= 0)
            return false;

        // Suchen wir mal das Bit
        for (int index = 0; index < m_bits.length; index++)
            if (m_bits[index] == v) {
                int action = event.getAction();

                // Zu Beginn einer Geste merken wir uns die horizontale Position
                if (action == MotionEvent.ACTION_DOWN) {
                    m_touchStart = event.getX();
                    return true;
                }

                // Ansonsten interessiert nur das Ende der Geste
                if (action != MotionEvent.ACTION_UP)
                    return false;

                // Wir reagieren erst ab einem willkürlich gewählten Mindestenabstand
                float moveX = event.getX() - m_touchStart;
                if (Math.abs(moveX) < 100)
                    return false;

                // Feedback für den Anwender starten
                startAnimation(index, moveX < 0);

                return true;
            }

        // Das ist keins von unseren Bits
        return false;
    }

    // Beginnt mit der Animation als Feedback für den Anwender.
    private void startAnimation(int index, boolean flingLeft) {
        m_currentAnimation = index;

        // Wir vereinfachen uns den gemeinsamen Start
        AnimationSet allBitAnimation = new AnimationSet(true);

        // Wir kümmern uns erst einmal um das vom Anwender bewegte Bit
        AnimationSet bitAnimation = new AnimationSet(true);
        allBitAnimation.addAnimation(bitAnimation);
        bitAnimation.setFillAfter(true);
        m_bits[index].setAnimation(bitAnimation);

        // Der Bewegung der Geste folgen und aus der Reihe ausscheren
        Animation flingOut = new TranslateAnimation(0, flingLeft ? -ANIMATION_OFFSET : ANIMATION_OFFSET, 0, 0);
        bitAnimation.addAnimation(flingOut);
        flingOut.setStartOffset(0);
        flingOut.setDuration(ANIMATION_TIME_FLING);

        // Zeitpunkt ermitteln
        long endOfTimeline = flingOut.getDuration();

        if (index < m_bits.length - 1) {
            // Das Fallen beginnt leicht verzögert
            endOfTimeline = ANIMATION_TIME_FALL_OFFSET;

            // Alles über uns verschieben
            for (int i = index; i < m_bits.length - 1; ) {
                // Pause zwischen den Bits einfügen
                if (i++ > index)
                    endOfTimeline += ANIMATION_TIME_FALL_DELAY - ANIMATION_TIME_FALL;

                // Die Bits fallen einfach herunter
                Animation fall = new TranslateAnimation(0, 0, 0, m_bits[i - 1].getY() - m_bits[i].getY());
                allBitAnimation.addAnimation(fall);
                fall.setStartOffset(endOfTimeline);
                fall.setDuration(ANIMATION_TIME_FALL);
                fall.setFillAfter(true);
                m_bits[i].setAnimation(fall);

                // Auf das Ende dieser Animation wollen wir warten
                registerAnimation(fall);

                // Zeit korrigieren
                endOfTimeline += fall.getDuration();
            }

            // Schauen wir mal, ob wir steigen müssen
            long riseTime = endOfTimeline - 2 * flingOut.getDuration();
            if (riseTime > 0) {
                Animation rise = new TranslateAnimation(0, 0, 0, m_bits[m_bits.length - 1].getY() - m_bits[index].getY());
                bitAnimation.addAnimation(rise);
                rise.setStartOffset(flingOut.getDuration());
                rise.setDuration(riseTime);
            }

            // Zeit entsprechend korrigieren
            endOfTimeline -= flingOut.getDuration();
        }

        // Zurück in die Reihe
        Animation flingIn = new TranslateAnimation(0, flingLeft ? ANIMATION_OFFSET : -ANIMATION_OFFSET, 0, 0);
        bitAnimation.addAnimation(flingIn);
        flingIn.setStartOffset(endOfTimeline);
        flingIn.setDuration(flingOut.getDuration());

        // Auf das Ende dieser Animation wollen wir auch warten
        registerAnimation(flingIn);

        // Alles zusammen vorbereiten
        allBitAnimation.start();

        // Die Animationen den visuellen Elementen zuordnen und loslaufen lassen
        for (int i = index; i < m_bits.length; i++)
            m_bits[i].startAnimation(m_bits[i].getAnimation());
    }

    // Die Animation der Bits ist abgeschlossen, wir können nun die gewünschte Verschiebung durchführen.
    private void finishAnimation() {
        // Alle Präsentationen werden an den korrekten Platz geschoben
        for (int i = m_currentAnimation; i < m_bits.length; i++)
            m_bits[i].clearAnimation();

        // Die Bitvertauschung werden vorgenommen
        m_currentRiddle.move(m_currentAnimation);

        // Neu zeichnen
        refresh();

        // Von nun an können wir neu anordnen
        m_currentAnimation = -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.riddle_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Solange die letzte Aktion nicht vollständig abgeschlossen ist, machen wir gar nichts
        if (m_currentAnimation >= 0)
            return super.onOptionsItemSelected(item);

        // Mal schauen, was der Anwender von uns will
        switch (item.getItemId()) {
            case R.id.action_new:
                // Mit neuer Zahl starten
                m_currentRiddle = new Riddle(m_numberOfBits, null);
                break;
            case R.id.action_reset:
                // Mit der selben Zahl und der selben Anordnung der Bits starten
                m_currentRiddle.restart();
                break;
            case R.id.action_settings:
                // Einstellungen anzeigen und das Ergebnis abwarten
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_RESULT);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        // Neu zeichnen
        refresh();
        return true;
    }

    // Der Anwender möchte die Hilfeseite sehen.
    public void onHelp(View starter) {
        Intent intent = new Intent();
        intent.setClass(this, HelpAndIntro.class);
        intent.putExtra(HelpAndIntro.EXTRA_NUMBER_OF_BITS, m_numberOfBits);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        m_currentRiddle.save(outState);
    }

    // Meldet eine zu überwachende Animation an.
    private void registerAnimation(Animation animation) {
        if (animation == null)
            return;

        m_pendingAnimations++;

        animation.setAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }


    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        // Wir reagieren nur, wenn alle Animationen abgeschlossen sind
        if (m_pendingAnimations < 1)
            return;
        if (m_pendingAnimations-- > 1)
            return;

        // Wir warten noch einen kleinen Moment bis wir Änderungen an der Oberfläche vornehmen
        m_bitContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAnimation();
            }
        }, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Nachsehen, ob die Einstellungen verändert wurden
        if (requestCode == SETTINGS_RESULT)
            if (resultCode == RESULT_OK)
                initialize(null);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Schauen wir einmal nach, ob wir ein Rätsel übermittelt bekommen haben
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
            startFromNdefMessage(intent);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // Wir machen uns hier das Leben etwas einfacher und verwenden die Java Bibliotheken
        ByteBuffer data = ByteBuffer.allocate(12);
        data.putInt(m_currentRiddle.NumberOfBits);
        data.putInt(m_currentRiddle.Goal);
        data.putInt(m_currentRiddle.FirstGuess);

        // Wir verpacken das nun in eine NCF Nachricht mit zusätzlichem AAR
        NdefMessage msg = new NdefMessage(new NdefRecord[]
                {
                        NdefRecord.createMime("application/vnd.net.psimarron.bitme.v0", data.array()),
                        NdefRecord.createApplicationRecord("net.psimarron.bitme")
                });

        // Und das senden wir an den Partner
        return msg;
    }

    // Initialisiert die Oberfläche aus einem Rätsel auf einem anderen SmartPhone
    private void startFromNdefMessage(Intent intent) {
        // Wir holen uns einfach blind die Daten - solang das Manifest korrekt aufgesetzt ist, kann es eigentlich keine Probleme geben
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (messages == null)
            return;
        if (messages.length < 1)
            return;

        NdefRecord[] records = ((NdefMessage) messages[0]).getRecords();
        if (records == null)
            return;
        if (records.length < 1)
            return;

        NdefRecord firstRecord = records[0];
        if (firstRecord == null)
            return;
        byte[] data = firstRecord.getPayload();
        if (data == null)
            return;
        if (data.length != 12)
            return;

        // Nun müssen wir das nur rekonstruieren
        ByteBuffer reader = ByteBuffer.wrap(data);
        int numberOfBits = reader.getInt();
        int goal = reader.getInt();
        int first = reader.getInt();

        // Schließlich tun wir einfach so, als wären wir aufgeweckt worden
        initialize(Riddle.toBundle(numberOfBits, goal, first));
    }
}
