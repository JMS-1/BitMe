package net.psimarron.bitme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

// Das ist die Aktivität mit dem eigentlichen Spiel.
public class TheRiddle extends Activity implements View.OnTouchListener {

    // Die Weite der horizontalen Verschiebung des ausgewählten Bits.
    private final int ANIMATION_OFFSET = 200;

    // Die Zeit in Millisekunden für die horizontale Verschiebung bis zur weitesten Entfernung.
    private final int ANIMATION_TIME_FLING = 200;

    // Die Startverzögerung in Millisekunden für den Beginn des freien Falls.
    private final int ANIMATION_TIME_FALL_OFFSET = ANIMATION_TIME_FLING / 4;

    // Die Startverzögerung in Millisekunden zwischen dem Fall der einzelnen Bits.
    private final int ANIMATION_TIME_FALL_DELAY = ANIMATION_TIME_FALL_OFFSET;

    // Die Fallzeit in Millisekunden für jedes einzelne Bit.
    private final int ANIMATION_TIME_FALL = 4 * ANIMATION_TIME_FLING - 2 * (ANIMATION_TIME_FALL_OFFSET + ANIMATION_TIME_FALL_DELAY);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Neu erzeugen oder rekonstruieren
        m_currentRiddle = new Riddle(8, savedInstanceState);
        m_numberOfBits = m_currentRiddle.NumberOfBits;

        // Die äußere visuelle Darstellung
        LayoutInflater inflater = getLayoutInflater();
        View content = inflater.inflate(R.layout.activity_the_riddle, null);

        setContentView(content);

        // Im Moment verwenden wir eine geschachtelte Darstellung zum Abblenden des Hintergrunds - TODO: vermutlich wäre das eher eine Aufgabe für ein LayerDrawable
        m_bitContainer = (ViewGroup) content.findViewById(R.id.game_container);

        // Die relevanten Oberflächenelemente
        m_guess = (TextView) findViewById(R.id.view_guess);
        m_bits = new View[m_numberOfBits];

        // Für jedes Bit wird dynamisch eine entsprechende Repräsentation erzeugt
        for (int i = 0; i < m_bits.length; i++) {
            // Am einfachsten geht das aus einer Vorlage - die zusätzliche Schachtelung erleichtert die Manipulation der LayoutParameter
            ViewGroup template = (ViewGroup) inflater.inflate(R.layout.bit, null);
            View bit = template.findViewById(R.id.bit_display);
            template.removeView(bit);

            // Die Anzeige des Bits vorbereiten
            bit.setOnTouchListener(this);

            // Und dann merken wir uns die Präsentation und schalten die Anzeige frei
            m_bitContainer.addView(m_bits[i] = bit, 0);
        }

        // Initiales zeichnen
        refresh();
    }

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

        // Alles zusammen vorbereiten
        allBitAnimation.start();

        // Auf das Ende warten
        flingIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                m_bitContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishAnimation();
                    }
                }, 10);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Die Animationen den visuellen Elementen zuordnen und loslaufen lassen
        for (int i = index; i < m_bits.length; i++)
            m_bits[i].startAnimation(m_bits[i].getAnimation());
    }

    private void finishAnimation() {
        // Alle Präsentationen werden an den korrekten Platz geschoben
        for (int i = m_currentAnimation; i < m_bits.length; i++)
            m_bits[i].setAnimation(null);

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
        switch (item.getItemId()) {
            case R.id.action_new:
                // Mit neuer Zahl starten
                m_currentRiddle = new Riddle(m_numberOfBits, null);
                break;
            case R.id.action_reset:
                // Mit der selben Zahl und der selben Anordnung der Bits starten
                m_currentRiddle.restart();
                break;
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
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        m_currentRiddle.save(outState);
    }
}
