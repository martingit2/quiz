package com.example.quizapp;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private int totalQuestions = 10; // Standard = 10, men kan endres via spillkonfigurasjon.
    private int score = 0;
    private String playerName;

    // Flaghåndtering
    private FlagManager flagManager;
    private List<Flag> flags;            // Alle flagg
    private List<Flag> activeFlags = new ArrayList<>(); // Flagg valgt i GameConfigPanel
    private List<Flag> unusedFlags;      // For å unngå gjentakelser i en runde
    private Flag currentFlag;

    // Scenes
    private Scene mainMenuScene;
    private Scene quizScene;

    // UI-komponenter for quiz-scenen
    private Label questionLabel;
    private Label scoreLabel;
    private ImageView flagView;
    private Button[] answerButtons;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

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

        // Standard: ingen flagg valgt i GameConfigPanel => bruk alle
        unusedFlags = new ArrayList<>(flags);

        // Bygg hovedmeny
        buildMainMenuScene();

        primaryStage.setTitle("Quiz App - " + playerName);
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();
    }

    /**
     * Bygger hovedmenyen med tre alternativer.
     */
    private void buildMainMenuScene() {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(20));

        Label titleLabel = new Label("Quiz App");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button startGameButton = new Button("Start nytt spill");
        Button gameConfigButton = new Button("Adminpanel - Spillkonfig");
        Button settingsButton = new Button("Innstillinger - Flaggadministrasjon");

        // Start quiz
        startGameButton.setOnAction(e -> startQuiz());

        // Åpner GameConfigPanel, der admin kan velge antall spørsmål + spesifikke flagg
        gameConfigButton.setOnAction(e -> {
            GameConfigPanel configPanel = new GameConfigPanel(flagManager, this);
            configPanel.show();
        });

        // Åpner flagg-administrasjon
        settingsButton.setOnAction(e -> new AdminPanel(flagManager).show());

        menuLayout.getChildren().addAll(titleLabel, startGameButton, gameConfigButton, settingsButton);
        mainMenuScene = new Scene(menuLayout, 600, 400);
    }

    /**
     * Starter en ny quiz.
     */
    private void startQuiz() {
        currentQuestion = 1;
        score = 0;

        // Hvis admin har valgt noen flagg i GameConfigPanel, bruk dem.
        // Ellers bruk alle (flags).
        List<Flag> quizFlags = (activeFlags != null && !activeFlags.isEmpty())
                ? activeFlags : flags;

        unusedFlags = new ArrayList<>(quizFlags);

        // Bygg quiz-scenen
        BorderPane quizLayout = new BorderPane();

        // Toppfelt: Spørsmål, tilbake-knapp, og poeng
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER);
        questionLabel = new Label("Spørsmål: " + currentQuestion + " / " + totalQuestions);
        scoreLabel = new Label("Poeng: " + score);
        Button backButton = new Button("Hovedmeny");
        backButton.setOnAction(e -> primaryStage.setScene(mainMenuScene));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(questionLabel, spacer, backButton, scoreLabel);
        quizLayout.setTop(topBar);

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
            answersGrid.add(answerButtons[i], i % 2, i / 2);
        }
        centerBox.getChildren().add(answersGrid);

        quizLayout.setCenter(centerBox);

        quizScene = new Scene(quizLayout, 600, 400);
        primaryStage.setScene(quizScene);

        updateQuestionWithAnimation();
    }

    /**
     * Oppdaterer spørsmålet med en fade-out/fade-in effekt.
     */
    private void updateQuestionWithAnimation() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), flagView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            updateQuestionContent();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), flagView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    /**
     * Oppdaterer innholdet i spørsmålet (velger nytt flagg og oppdaterer svaralternativer).
     */
    private void updateQuestionContent() {
        if (currentQuestion > totalQuestions) {
            finishQuiz();
            return;
        }
        questionLabel.setText("Spørsmål: " + currentQuestion + " / " + totalQuestions);

        // Hvis vi går tom for flagg, reinitialiser
        if (unusedFlags.isEmpty()) {
            unusedFlags = new ArrayList<>(getActiveFlagsOrAll());
        }

        Random rand = new Random();
        int index = rand.nextInt(unusedFlags.size());
        currentFlag = unusedFlags.remove(index);
        flagView.setImage(currentFlag.getImage());

        // Lag svaralternativer: korrekt svar + distraktorer
        List<String> alternatives = new ArrayList<>();
        alternatives.add(currentFlag.getCountry());

        // Distraktorer fra enten activeFlags eller flags
        List<Flag> distractors = new ArrayList<>(getActiveFlagsOrAll());
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
     * Returnerer enten de admin-valgte flaggene, eller alle, hvis ingen er valgt.
     */
    private List<Flag> getActiveFlagsOrAll() {
        return (activeFlags != null && !activeFlags.isEmpty()) ? activeFlags : flags;
    }

    /**
     * Håndterer svar når en knapp trykkes med animasjon for korrekt/feil svar.
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
     * Fullfører quizen, logger resultatet og viser high score.
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
        Platform.runLater(alert::showAndWait);

        // Etter quiz returneres man til hovedmenyen
        primaryStage.setScene(mainMenuScene);
    }

    // -----------------------------------------
    // GET/SET FOR totalQuestions og activeFlags
    // -----------------------------------------
    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int newTotal) {
        this.totalQuestions = newTotal;
    }

    public void setActiveFlags(List<Flag> selectedFlags) {
        this.activeFlags = selectedFlags;
    }

    public List<Flag> getActiveFlags() {
        return activeFlags;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
