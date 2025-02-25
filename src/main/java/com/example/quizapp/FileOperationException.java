package com.example.quizapp;

/**
 * Et spesifikt unntak for filoperasjoner.
 */
public class FileOperationException extends QuizException {
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
