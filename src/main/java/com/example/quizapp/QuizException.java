package com.example.quizapp;

/**
 * En generell unntaksklasse for Quiz-applikasjonen.
 */
public class QuizException extends Exception {
    public QuizException(String message) {
        super(message);
    }

    public QuizException(String message, Throwable cause) {
        super(message, cause);
    }
}
