package net.psimarron.bitme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

// Diese Ableitung versucht zu verhindern, dass der ScrollView mit dem Spiel interferiert.
public class VerticalOnlyScrollView extends ScrollView {
    // Horizontale Anfangsposition einer Geste.
    private float m_biasX;

    // Vertikale Anfangsposition einer Geste.
    private float m_biasY;

    // Aufsummierte horizontale Bewegung.
    private float m_totalX;

    // Aufsimmierte vertikale Bewegung.
    private float m_totalY;

    // Wir können nur aus XML heraus erzeugt werden.
    public VerticalOnlyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Anfang der Geste
                m_biasX = ev.getX();
                m_biasY = ev.getY();
                m_totalX = 0;
                m_totalY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                // Wir versuchen herauszubekommen, ob wir eine eher horizontale Bewegung haben.
                m_totalX += Math.abs(ev.getX() - m_biasX);
                m_totalY += Math.abs(ev.getY() - m_biasY);
                m_biasX = ev.getX();
                m_biasY = ev.getY();

                // Wir versuchen herauszubekommen, ob wir eine eher horizontale Bewegung haben.
                if (10000 * m_totalX >= m_totalY)
                    return false;
        }

        return super.onInterceptTouchEvent(ev);
    }
}