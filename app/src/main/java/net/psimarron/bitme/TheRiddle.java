package net.psimarron.bitme;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TheRiddle extends Activity implements Riddle.ChangeListener, View.OnTouchListener {

    private Riddle m_currentRiddle;

    private TextView m_guess;

    private TextView[] m_bits;

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
            TextView bit = (TextView) inflater.inflate(R.layout.bit, null);

            bit.setTag(new Integer((Riddle.NUMBER_OF_BITS - 1) - i));
            bit.setOnTouchListener(this);

            view.addView(m_bits[i] = bit);
        }

        newRiddle();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_the_riddle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (action == MotionEvent.ACTION_DOWN)
            return true;
        if (action != MotionEvent.ACTION_UP)
            return false;

        Integer index = (Integer) v.getTag();
        if (index == null)
            return false;

        m_currentRiddle.move(index);

        return true;
    }
}
