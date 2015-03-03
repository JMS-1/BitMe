package net.psimarron.bitme;

import java.util.Random;

public class Riddle {

    public static final int NUMBER_OF_BITS = 10;
    private final static Random s_generator = new Random();
    private final int m_number;
    private final ChangeListener m_listener;
    private int m_guess;

    public Riddle(ChangeListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener");

        // Hier melden wir alle Änderungen
        m_listener = listener;

        // Irgend eine Zahl
        m_number = s_generator.nextInt(1 << NUMBER_OF_BITS);

        // Das ist auch der erste Rateversuch
        m_guess = m_number;

        // Und dann Würfeln wir das ordentlich durcheinander
        for (int n = NUMBER_OF_BITS * 2; n-- > 0; ) {
            int i = s_generator.nextInt(NUMBER_OF_BITS - 1);

            swap(i, i + 1);
        }

        // Die erste Änderung
        m_listener.onGuessChanged(this);
    }

    public void move(int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NUMBER_OF_BITS)
            throw new IllegalArgumentException("i");

        int guess = m_guess;

        int mask = 1 << i;
        int bit = (guess & mask) >> i;
        int right = guess & (mask - 1);
        int left = guess >> (i + 1);

        m_guess = (bit << (NUMBER_OF_BITS - 1)) | (left << i) | right;

        if (guess != m_guess)
            m_listener.onGuessChanged(this);
    }

    public boolean get(int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NUMBER_OF_BITS)
            throw new IllegalArgumentException("i");

        return (m_guess & (1 << i)) != 0;
    }

    private void swap(int i, int j) {
        int iMask = 1 << i;
        int jMask = 1 << j;

        int iBit = (m_guess & iMask) >> i;
        int jBit = (m_guess & jMask) >> j;

        m_guess = ((m_guess & ~iMask) & ~jMask) | (iBit << j) | (jBit << i);
    }

    public boolean isMatch() {
        for (int i = 0, mask = 1; i < NUMBER_OF_BITS; i++, mask += mask)
            if ((m_number & mask) != (m_guess & mask))
                return false;

        return true;
    }

    public int getGoal() {
        return m_number;
    }

    public int getGuess() {
        return m_guess;
    }

    public interface ChangeListener {
        void onGuessChanged(Riddle riddle);
    }
}
