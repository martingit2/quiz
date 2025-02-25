package com.example.quizapp;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizApp extends Application {

    // Quiz-logikk
    private int currentQuestion = 1;
    private final int totalQuestions = 10;
    private int score = 0;
    private String playerName;

    // Flaghåndtering
    private FlagManager flagManager;
    private List<Flag> flags;
    private List<Flag> unusedFlags; // Liste med flagg som ennå ikke er brukt i runden.
    private Flag currentFlag;

    // UI-komponenter
    private Label questionLabel;
    private Label scoreLabel;
    private ImageView flagView;
    private Button[] answerButtons;

    @Override
    public void start(Stage primaryStage) {
        // Spør bruker om navn
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Quiz App");
        nameDialog.setHeaderText("Velkommen til Quiz App");
        nameDialog.setContentText("Skriv inn navn:");
        nameDialog.showAndWait().ifPresent(name -> playerName = name);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Spiller";
        }

        // Initialiser flagg-data
        flagManager = new FlagManager();
        flags = flagManager.getFlags();
        // Lag en kopi av alle flagg for denne quizen slik at vi kan unngå å bruke det samme flagget mer enn én gang.
        unusedFlags = new ArrayList<>(flags);

        // Opprett hovedlayout med BorderPane
        BorderPane root = new BorderPane();

        // Toppfelt: Spørsmål, admin-knapp og poeng
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER);
        questionLabel = new Label("Spørsmål: " + currentQuestion + " / " + totalQuestions);
        Button adminButton = new Button("Admin");
        adminButton.setOnAction(e -> {
            AdminPanel adminPanel = new AdminPanel(flagManager);
            adminPanel.show();
        });
        scoreLabel = new Label("Poeng: " + score);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(questionLabel, spacer, adminButton, scoreLabel);
        root.setTop(topBar);

        // Sentrumsdel: Flaggbilde og svaralternativer
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        flagView = new ImageView();
        flagView.setFitWidth(200);
        flagView.setPreserveRatio(true);
        centerBox.getChildren().add(flagView);

        GridPane answersGrid = new GridPane();
        answersGrid.setHgap(20);
        answersGrid.setVgap(20);
        answersGrid.setAlignment(Pos.CENTER);

        answerButtons = new Button[4];
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i] = new Button();
            answerButtons[i].setMinWidth(100);
            int index = i;
            answerButtons[i].setOnAction(e -> handleAnswer(answerButtons[index].getText()));
        }
        answersGrid.add(answerButtons[0], 0, 0);
        answersGrid.add(answerButtons[1], 1, 0);
        answersGrid.add(answerButtons[2], 0, 1);
        answersGrid.add(answerButtons[3], 1, 1);
        centerBox.getChildren().add(answersGrid);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Quiz App - " + playerName);
        primaryStage.setScene(scene);
        primaryStage.show();

        updateQuestionWithAnimation();
    }

    /**
     * Oppdaterer spørsmålet med en fade-out/fade-in effekt.
     */
    private void updateQuestionWithAnimation() {
        // Først fade ut flaggbildet
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), flagView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            updateQuestionContent();
            // Deretter fade inn flaggbildet
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), flagView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Oppdaterer innholdet i spørsmålet (valg av nytt flagg, oppdatering av svaralternativer).
     */
    private void updateQuestionContent() {
        if (currentQuestion > totalQuestions) {
            finishQuiz();
            return;
        }

        questionLabel.setText("Spørsmål: " + currentQuestion + " / " + totalQuestions);
        // Sørg for at vi ikke går tom for spørsmål. Hvis listen er tom, kan vi reinitialisere den,
        // men det betyr at spørsmål kan gjentas dersom antallet spørsmål i quizen overstiger antall unike flagg.
        if (unusedFlags.isEmpty()) {
            unusedFlags = new ArrayList<>(flags);
        }
        Random rand = new Random();
        int index = rand.nextInt(unusedFlags.size());
        currentFlag = unusedFlags.remove(index);
        flagView.setImage(currentFlag.getImage());

        // Lag svaralternativer: korrekt svar + distraktorer
        List<String> alternatives = new ArrayList<>();
        alternatives.add(currentFlag.getCountry());
        List<Flag> distractors = new ArrayList<>(flags);
        distractors.remove(currentFlag);
        Collections.shuffle(distractors);
        for (int i = 0; i < 3 && i < distractors.size(); i++) {
            alternatives.add(distractors.get(i).getCountry());
        }
        Collections.shuffle(alternatives);
        for (int i = 0; i < answerButtons.length; i++) {
            answerButtons[i].setText(alternatives.get(i));
        }
    }

    /**
     * Håndterer svar når en knapp trykkes med animasjon for korrekt/feil svar.
     * @param selectedAnswer teksten på knappen som ble trykket
     */
    private void handleAnswer(String selectedAnswer) {
        if (selectedAnswer.equals(currentFlag.getCountry())) {
            score += 10;
            animateCorrectAnswer();
        } else {
            animateIncorrectAnswer();
        }
        scoreLabel.setText("Poeng: " + score);
        currentQuestion++;
        updateQuestionWithAnimation();
    }

    /**
     * Animerer en scale-effekt for korrekt svar.
     */
    private void animateCorrectAnswer() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), flagView);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.2);
        scale.setToY(1.2);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    /**
     * Animerer en shake-effekt for feil svar.
     */
    private void animateIncorrectAnswer() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), flagView);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Fullfører quizen, logger resultat og viser high score.
     */
    private void finishQuiz() {
        HighScoreManager highScoreManager = new HighScoreManager();
        highScoreManager.addHighScore(new HighScoreEntry(playerName, score));
        QuizLogger.logQuizResult(playerName, score);

        StringBuilder sb = new StringBuilder("Gratulerer " + playerName + "!\nDin poengsum er: " + score + "\n\nHigh Scores:\n");
        for (HighScoreEntry entry : highScoreManager.getHighScores()) {
            sb.append(entry).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz ferdig!");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());

        // Bruk Platform.runLater for å vise alerten etter animasjon/layout-prosessering
        Platform.runLater(() -> alert.showAndWait());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
