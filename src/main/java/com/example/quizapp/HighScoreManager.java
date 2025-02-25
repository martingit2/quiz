package com.example.quizapp;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private List<HighScoreEntry> highScores;
    private final String FILENAME = "highscores.ser";
    private final int MAX_ENTRIES = 5;

    public HighScoreManager() {
        highScores = loadHighScores();
        if (highScores == null) {
            highScores = new ArrayList<>();
        }
    }

    public List<HighScoreEntry> getHighScores() {
        return highScores;
    }

    public void addHighScore(HighScoreEntry entry) {
        highScores.add(entry);
        Collections.sort(highScores);
        if (highScores.size() > MAX_ENTRIES) {
            highScores = highScores.subList(0, MAX_ENTRIES);
        }
        saveHighScores();
    }

    private void saveHighScores() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            out.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HighScoreEntry> loadHighScores() {
        File file = new File(FILENAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<HighScoreEntry>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
