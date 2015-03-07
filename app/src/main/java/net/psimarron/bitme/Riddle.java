package net.psimarron.bitme;

import java.util.Random;

// Eine Instanz dieser Klasse beschreibt ein einzelnes Rätsel.
public class Riddle {
    // 8 Bits reichen erst einmal - es soll ja für Anfänger sein und wenn man das Prinzip verstanden hat, ist es eh egal.
    public static final int NUMBER_OF_BITS = 8;

    // Stellt Zufallszahlen bereit.
    private final static Random s_generator = new Random();

    // Die zu ratende Zahl.
    public final int Goal;

    // Die minimale Anzahl von Verschiebungen.
    public final int Par;

    // Informiert über Änderungen.
    private final ChangeListener m_listener;

    // Der erste Rateversuch.
    private final int m_firstGuess;

    // Der aktuelle Rateversuch.
    private int m_guess;

    // Die Anzahl der Versuche.
    private int m_tries;

    // Erstellt ein neues Rätsel.
    public Riddle(ChangeListener listener) {
        // Irgend eine Zahl, nur nicht alles 0 oder alles 1, sonst gibt es keinen abweichenden Anfangsratewert
        Goal = 1 + s_generator.nextInt((1 << NUMBER_OF_BITS) - 2);

        // Das ist auch der erste Rateversuch
        m_guess = Goal;

        // Und dann würfeln wir das ordentlich durcheinander
        for (int n = NUMBER_OF_BITS * 2; n-- > 0; ) {
            int i = s_generator.nextInt(NUMBER_OF_BITS - 1);

            // Naja, einfach ein paar Mal Bits vertauschen
            swap(i, i + 1);
        }

        // Wenn es jetzt schon passt müssen wir korrigieren ansonsten
        if (isMatch())
            move(0);

        // Minimale Anzahl von Versuchen ermitteln
        Par = RiddleAnalyser.getPar(this);

        // Anfangsstand merken
        m_listener = listener;
        m_firstGuess = m_guess;

        // Und melden
        onGuessChanged();
    }

    // Verschiebt eine einzelne Bitposition.
    public static int move(int number, int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NUMBER_OF_BITS)
            throw new IllegalArgumentException("i");

        // So war es vorher
        int guess = number;

        // Der alte Wert hat so in etwa das Format <unberührter linker Teil> <gewählte Position> <inberührter rechter Teil>
        int mask = 1 << i;
        int bit = (guess & mask) >> i;
        int right = guess & (mask - 1);
        int left = guess >> (i + 1);

        // Das bauen wir dann um in <gewählte Position> <unberührter linker Teil> <inberührter rechter Teil>
        return (bit << (NUMBER_OF_BITS - 1)) | (left << i) | right;
    }

    // Der Spieler hat eine Bitposition ausgewählt, die dann entfernt und deren Wert ganz nach oben gesetzt wird.
    public void move(int i) {
        // So war es vorher
        int guess = m_guess;

        // So ist es nachher - wir zählen auch die Versuche
        m_guess = move(guess, i);
        m_tries++;

        // Nur wenn sich etwas verändert hat müssen wir auch die Anzeige erneuern
        if (guess != m_guess)
            onGuessChanged();
    }

    // Meldet eine Veränderung am Ratestand
    private void onGuessChanged() {
        if (m_listener != null)
            m_listener.onGuessChanged(this);
    }

    // Meldet ein Bit des aktuell geratenden Wertes.
    public boolean get(int i) {
        if (i < 0)
            throw new IllegalArgumentException("i");
        if (i >= NUMBER_OF_BITS)
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

    // Meldet den aktuellen Ratewert.
    public int getGuess() {
        return m_guess;
    }

    // Meldet die Anzahl der Versuche.
    public int getTries() {
        return m_tries;
    }

    // Das selbe Rätsel von vorne.
    public void restart() {
        // Hm, da sind wir aber schon
        if (m_guess == m_firstGuess)
            return;

        // Zurücksetzen
        m_guess = m_firstGuess;
        m_tries = 0;

        // Und melden
        onGuessChanged();
    }

    // Über diese Schnittstelle erfolgen alle Benachrichtigungen.
    public interface ChangeListener {
        void onGuessChanged(Riddle riddle);
    }
}
