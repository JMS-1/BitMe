package net.psimarron.bitme;

import java.util.Hashtable;

// Hier der Algorithmus, der auf relative einfache Art zu einer Zahl und einer zugehörigen Vertauschung von Bits die minimale Anzahl von Vertauschungen zur Rekonstruktion berechnet.
final class RiddleAnalyser {
    // Alles, was wir schon mal untersucht haben.
    private final Hashtable<Integer, Integer> m_inspected = new Hashtable<Integer, Integer>();

    // Das ist die Zahl, die geraten werden soll.
    private final int m_target;

    // Erstellt einen neuen Algorithmus.
    private RiddleAnalyser(int target) {
        m_target = target;
    }

    // Ermittelt zu einem Rätsel die minimale Anzahl der notwendigen Bitvertauschungen.
    public static int getPar(Riddle riddle) {
        // Analyseumgebung aufsetzen
        RiddleAnalyser analyser = new RiddleAnalyser(riddle.Goal);

        // Analyse durchführen
        return analyser.analyse(0, riddle.getGuess());
    }

    // Führt eine Überprüfung auf einer Ebene durch.
    private int analyse(int depth, int number) {
        // Ziel erreicht
        if (number == m_target)
            return depth;

        // Mehr als 8 Vertauschungen machen keinen Sinn
        if (depth == 8)
            return -1;

        // Schauen wir mal, ob wir den schon mal angeschaut haben
        Integer previousDepth = m_inspected.get(number);
        if ((previousDepth != null) && (depth >= previousDepth))
            return -1;
        else
            m_inspected.put(number, depth);

        // Das Beste, was wir gefunden haben
        int best = -1;

        // Und gehen eine Ebene tiefer
        for (int bit = 0; bit < Riddle.NUMBER_OF_BITS - 1; bit++) {
            // Weiter auswerten - technisch könnte man die Rekursion auch leicht durch eine lineare Variante bauen, aber so ist es einfacher zu verstehen
            int goal = analyse(depth + 1, Riddle.move(number, bit));
            if (goal >= 0)
                if (best == -1)
                    best = goal;
                else if (goal < best)
                    best = goal;
        }

        // Geringste Tiefe melden
        return best;
    }
}
