package net.psimarron.bitme;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class TheRiddle extends Activity implements Riddle.ChangeListener, View.OnTouchListener {

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

    private Riddle m_currentRiddle;

    private TextView m_guess;

    private TextView[] m_bits;
    private float m_touchStart;

    private int m_currentAnimation = -1;

    // Erstellt ein neues Rätsel.
    private void newRiddle() {
        m_currentRiddle = new Riddle(this);

        // Wir passen auch die Überschrift entsprechend an
        setTitle(getResources().getString(R.string.app_title, m_currentRiddle.Goal));

        // Und den Hinweis auf die minimal benötigte Anzahl von Verschiebungen
        m_guess.setText(Integer.toString(m_currentRiddle.Par));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.activity_the_riddle, null);

        setContentView(view);

        m_guess = (TextView) findViewById(R.id.view_guess);
        m_bits = new TextView[Riddle.NUMBER_OF_BITS];

        for (int i = 0; i < m_bits.length; i++) {
            RelativeLayout template = (RelativeLayout) inflater.inflate(R.layout.bit, null);
            TextView bit = (TextView) template.findViewById(R.id.bit_display);

            template.removeView(bit);

            bit.setId(R.id.view_guess + 1 + i);
            bit.setTag(new Integer(i));
            bit.setOnTouchListener(this);

            RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) bit.getLayoutParams();

            layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
            if (i == m_bits.length - 1)
                layout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            else
                layout.addRule(RelativeLayout.BELOW, bit.getId() + 1);

            view.addView(m_bits[i] = bit);
        }

        newRiddle();
    }

    @Override
    public void onGuessChanged(Riddle riddle) {
        for (int i = 0; i < m_bits.length; i++) {
            TextView bit = m_bits[i];
            Integer index = (Integer) bit.getTag();

            bit.setText(riddle.get(index) ? "1" : "0");
        }

        if (!riddle.isMatch()) {
            // Im Wesentlichen weiter machen
            m_guess.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        // Ergebnis visualisieren
        if (riddle.getTries() > riddle.Par)
            m_guess.setBackgroundColor(Color.RED);
        else
            m_guess.setBackgroundColor(Color.GREEN);

        won();
    }

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

        int action = event.getAction();

        // Zu Beginn einer Geste merken wir uns die horizontale Position
        if (action == MotionEvent.ACTION_DOWN) {
            m_touchStart = event.getX();
            return true;
        }

        // Ansonsten interessiert nur das Ende der Geste
        if (action != MotionEvent.ACTION_UP)
            return false;

        // Jedes Bitelement enthält die laufenden Nummer des angezeigten Bits
        Integer index = (Integer) v.getTag();
        if (index == null)
            return false;

        // Wir reagieren erst ab einem willkürlich gewählten Mindestenabstand
        float moveX = event.getX() - m_touchStart;
        if (Math.abs(moveX) < 100)
            return false;

        // Feedback für den Anwender starten
        startAnimation(index, moveX < 0);

        return true;
    }

    // Beginnt mit der Animation als Feedback für den Anwender.
    private void startAnimation(int index, boolean flingLeft) {
        m_currentAnimation = index;

        // Wir vereinfachen uns den gemeinsamen Start
        AnimationSet allBitAnimation = new AnimationSet(true);

        // Wir kümmern uns erst einmal um das vom Anwender bewegte Bit
        AnimationSet bitAnimation = new AnimationSet(true);
        allBitAnimation.addAnimation(bitAnimation);
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
                int index = m_currentAnimation;

                for (int i = index; i < m_bits.length; i++)
                    m_bits[i].clearAnimation();

                m_currentAnimation = -1;
                m_currentRiddle.move(index);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Die Animationen den visuellen Elementen zuordnen
        for (int i = index; i < m_bits.length; i++)
            m_bits[i].startAnimation(m_bits[i].getAnimation());
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
                newRiddle();
                return true;
            case R.id.action_reset:
                m_currentRiddle.restart();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
