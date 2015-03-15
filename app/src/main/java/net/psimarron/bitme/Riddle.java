package net.psimarron.bitme;

import android.os.Bundle;

import java.util.Random;

// Eine Instanz dieser Klasse beschreibt ein einzelnes Rätsel.
public class Riddle {
    // Stellt Zufallszahlen bereit.
    private final static Random s_generator = new Random();
    // Der Name der Ablage für die Anzahl der Bits.
    private final static String STATE_NUMBER_OF_BITS = "numberOfBits";
    // Der Name der Ablage für die Zielzahl.
    private final static String STATE_GOAL = "goal";
    // Der Name der Ablage für den aktuellen Rateversuch.
    private final static String STATE_CURRENT = "guess";
    // Der Name der Ablage für den ersten Rateversuch.
    private final static String STATE_FIRST = "firstGuess";
    // Der Name der Ablage für die berits vorgenommenen Vertauschungen.
    private final static String STATE_TRIES = "tries";
    // 8 Bits reichen erst einmal - es soll ja für Anfänger sein und wenn man das Prinzip verstanden hat, ist es eh egal.
    public final int NumberOfBits;
    // Die zu ratende Zahl.
    public final int Goal;
    // Die minimale Anzahl von Verschiebungen.
    public final int Par;
    // Der erste Rateversuch.
    private final int m_firstGuess;

    // Der aktuelle Rateversuch.
    private int m_guess;

    // Die Anzahl der Versuche.
    private int m_tries;

    // Erstellt ein neues Rätsel.
    public Riddle(int numberOfBits, Bundle bundle) {
        if (bundle == null) {
            // Alles neu
            NumberOfBits = numberOfBits;

            // Irgend eine Zahl, nur nicht alles 0 oder alles 1, sonst gibt es keinen abweichenden Anfangsratewert
            Goal = 1 + s_generator.nextInt((1 << NumberOfBits) - 2);

            // Das ist auch der erste Rateversuch
            m_guess = Goal;

            // Und dann würfeln wir das ordentlich durcheinander
            for (int n = NumberOfBits * 2; n-- > 0; ) {
                int i = s_generator.nextInt(NumberOfBits - 1);

                // Naja, einfach ein paar Mal Bits vertauschen
                swap(i, i + 1);
            }

            // Wenn es jetzt schon passt müssen wir korrigieren
            if (isMatch())
                move(0);

            // Das ist der Anfangswert und die Zahl der Versuche ist immer 0
            m_firstGuess = m_guess;
            m_tries = 0;
        } else {
            // Rekonstruieren
            NumberOfBits = bundle.getInt(STATE_NUMBER_OF_BITS);
            m_firstGuess = bundle.getInt(STATE_FIRST);
            m_guess = bundle.getInt(STATE_CURRENT);
            m_tries = bundle.getInt(STATE_TRIES);
            Goal = bundle.getInt(STATE_GOAL);
        }

        // Minimale Anzahl von Versuchen ermitteln
        Par = RiddleAnalyser.getPar(this, m_firstGuess);
    }

    // Überträgt dieses Rätsel in die Ablage.
    public void save(Bundle bundle) {
        bundle.putInt(STATE_NUMBER_OF_BITS, NumberOfBits);
        bundle.putInt(STATE_FIRST, m_firstGuess);
        bundle.putInt(STATE_CURRENT, m_guess);
        bundle.putInt(STATE_TRIES, m_tries);
        bundle.putInt(STATE_GOAL, Goal);
    }

    // Verschiebt eine einzelne Bitposition.
    public int move(int number, int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NumberOfBits)
            throw new IllegalArgumentException("i");

        // So war es vorher
        int guess = number;

        // Der alte Wert hat so in etwa das Format <unberührter linker Teil> <gewählte Position> <inberührter rechter Teil>
        int mask = 1 << i;
        int bit = (guess & mask) >> i;
        int right = guess & (mask - 1);
        int left = guess >> (i + 1);

        // Das bauen wir dann um in <gewählte Position> <unberührter linker Teil> <inberührter rechter Teil>
        return (bit << (NumberOfBits - 1)) | (left << i) | right;
    }

    // Der Spieler hat eine Bitposition ausgewählt, die dann entfernt und deren Wert ganz nach oben gesetzt wird.
    public void move(int i) {
        m_guess = move(m_guess, i);
        m_tries++;
    }

    // Meldet ein Bit des aktuell geratenden Wertes.
    public boolean get(int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NumberOfBits)
            throw new IllegalArgumentException("i");

        return (m_guess & (1 << i)) != 0;
    }

    // Vertaucht ein Bit mit einem anderen.
    private void swap(int i, int j) {
        int iMask = 1 << i;
        int jMask = 1 << j;

        int iBit = (m_guess & iMask) >> i;
        int jBit = (m_guess & jMask) >> j;

        // Bits entfernen und vertauscht wieder einsetzen
        m_guess = ((m_guess & ~iMask) & ~jMask) | (iBit << j) | (jBit << i);
    }

    // Meldet, ob der Zielwert zusammengebaut wurde.
    public boolean isMatch() {
        return (m_guess == Goal);
    }

    // Meldet die Anzahl der Versuche.
    public int getTries() {
        return m_tries;
    }

    // Das selbe Rätsel von vorne.
    public void restart() {
        // Zurücksetzen
        m_guess = m_firstGuess;
        m_tries = 0;
    }
}
