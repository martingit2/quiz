package com.example.quizapp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class QuizLogger {
    private static final String LOG_FILE = "quiz_history.txt";

    public static void logQuizResult(String playerName, int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String logEntry = LocalDateTime.now() + " - " + playerName + " - " + score;
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
