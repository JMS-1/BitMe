package net.psimarron.bitme;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TheRiddle extends Activity implements Riddle.ChangeListener, View.OnTouchListener {

    private Riddle m_currentRiddle;

    private TextView m_guess;

    private TextView[] m_bits;
    private float m_touchStart;

    private void newRiddle() {
        m_currentRiddle = new Riddle(this);
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
        setTitle(Integer.toString(riddle.getGoal()));

        m_guess.setText(Integer.toString(riddle.getGuess()));

        for (int i = 0; i < m_bits.length; i++) {
            TextView bit = m_bits[i];
            Integer index = (Integer) bit.getTag();

            bit.setText(riddle.get(index) ? "1" : "0");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            m_touchStart = event.getX();
            return true;
        }

        if (action != MotionEvent.ACTION_UP)
            return false;

        Integer index = (Integer) v.getTag();
        if (index == null)
            return false;

        float moveX = event.getX() - m_touchStart;
        if (Math.abs(moveX) < 100)
            return false;

        m_currentRiddle.move(index);

        return true;
    }
}
