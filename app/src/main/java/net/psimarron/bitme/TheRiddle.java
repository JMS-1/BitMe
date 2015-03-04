package net.psimarron.bitme;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;


public class TheRiddle extends Activity implements Riddle.ChangeListener, View.OnTouchListener, Animation.AnimationListener {

    private Riddle m_currentRiddle;

    private TextView m_guess;

    private TextView[] m_bits;
    private float m_touchStart;

    private int m_currentAnimation = -1;

    // Erstellt ein neues Rätsel.
    private void newRiddle() {
        m_currentRiddle = new Riddle(this);

        // Wir passen auch die Überschrift entsprechend an
        setTitle(getResources().getString(R.string.app_title, m_currentRiddle.getGoal()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.activity_the_riddle, null);

        setContentView(view);

        m_guess = (TextView) findViewById(R.id.view_guess);
        m_bits = new TextView[Riddle.NUMBER_OF_BITS];

        for (int i = 0; i < m_bits.length; i++) {
            ViewGroup template = (ViewGroup) inflater.inflate(R.layout.bit, null);
            TextView bit = (TextView) template.findViewById(R.id.bit_display);

            bit.setTag(new Integer(i));
            bit.setOnTouchListener(this);

            template.removeView(bit);
            view.addView(m_bits[i] = bit, 0);
        }

        newRiddle();
    }

    @Override
    public void onGuessChanged(Riddle riddle) {
        m_guess.setText(Integer.toString(riddle.getGuess()));

        for (int i = 0; i < m_bits.length; i++) {
            TextView bit = m_bits[i];
            Integer index = (Integer) bit.getTag();

            bit.setText(riddle.get(index) ? "1" : "0");
        }
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

        AnimationSet animations = new AnimationSet(true);

        Animation fling = new TranslateAnimation(0, flingLeft ? -200 : 200, 0, 0);
        fling.setDuration(200);
        fling.setAnimationListener(this);

        animations.addAnimation(fling);
        animations.start();

        m_bits[index].startAnimation(fling);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        int index = m_currentAnimation;

        m_currentAnimation = -1;
        m_currentRiddle.move(index);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }
}
