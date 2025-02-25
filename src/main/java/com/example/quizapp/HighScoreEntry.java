package com.example.quizapp;

import java.io.Serializable;

public class HighScoreEntry implements Serializable, Comparable<HighScoreEntry> {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private int score;

    public HighScoreEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    // Sortering i synkende rekkefølge (høyest score først)
    @Override
    public int compareTo(HighScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        return playerName + " - " + score;
    }
}
