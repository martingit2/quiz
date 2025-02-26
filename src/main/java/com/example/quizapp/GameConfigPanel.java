package com.example.quizapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;


import java.util.ArrayList;
import java.util.List;

public class GameConfigPanel {

    private final FlagManager flagManager;
    private final QuizApp quizApp;

    // Alle tilgjengelige flagg
    private ObservableList<Flag> availableFlags;
    // De valgte flaggene (spørsmålene)
    private ObservableList<Flag> selectedFlags;

    public GameConfigPanel(FlagManager flagManager, QuizApp quizApp) {
        this.flagManager = flagManager;
        this.quizApp = quizApp;
        this.availableFlags = FXCollections.observableArrayList(flagManager.getFlags());
        this.selectedFlags = FXCollections.observableArrayList();
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Spillkonfigurasjon");

        // Label for overskrift
        Label headerLabel = new Label("1) Velg antall spørsmål. 2) Velg eksakt så mange flagg.");

        // Tekstfelt for antall spørsmål
        TextField questionsField = new TextField(String.valueOf(quizApp.getTotalQuestions()));
        questionsField.setPromptText("Antall spørsmål");

        // Knapp for å "Lagre antall" (frivillig, du kan også bare lese fra feltet ved OK)
        Button setQuestionsButton = new Button("Lagre antall");
        setQuestionsButton.setOnAction(e -> {
            try {
                int newTotal = Integer.parseInt(questionsField.getText().trim());
                if (newTotal <= 0) {
                    showError("Antall spørsmål må være større enn 0.");
                } else {
                    quizApp.setTotalQuestions(newTotal);
                    showInfo("OK", "Antall spørsmål er satt til " + newTotal);
                }
            } catch (NumberFormatException ex) {
                showError("Vennligst skriv inn et gyldig tall for antall spørsmål.");
            }
        });

        HBox questionsBox = new HBox(10, new Label("Antall spørsmål:"), questionsField, setQuestionsButton);

        // Opprett to ListViews: availableListView (venstre) og selectedListView (høyre)
        ListView<Flag> availableListView = new ListView<>(availableFlags);
        availableListView.setPrefSize(200, 400);
        availableListView.setCellFactory(lv -> new ListCell<Flag>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Flag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getCountry());
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(item.getImage());
                    setGraphic(imageView);
                }
            }
        });

        ListView<Flag> selectedListView = new ListView<>(selectedFlags);
        selectedListView.setPrefSize(200, 400);
        selectedListView.setCellFactory(lv -> new ListCell<Flag>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Flag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getCountry());
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(item.getImage());
                    setGraphic(imageView);
                }
            }
        });

        // Tillat multiple selection
        availableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Knapper for å flytte flagg mellom listene
        Button addButton = new Button("Legg til ->");
        addButton.setOnAction(e -> {
            int totalQ = quizApp.getTotalQuestions(); // Hvor mange spørsmål admin har satt
            List<Flag> selected = new ArrayList<>(availableListView.getSelectionModel().getSelectedItems());
            if (selected.isEmpty()) {
                showError("Ingen flagg valgt i venstre liste.");
                return;
            }
            // Sjekk om vi har plass til å legge til dem
            if (selectedFlags.size() + selected.size() > totalQ) {
                showError("Kan ikke legge til flere enn " + totalQ + " flagg (du har allerede valgt " + selectedFlags.size() + ").");
                return;
            }
            availableFlags.removeAll(selected);
            selectedFlags.addAll(selected);
        });

        Button removeButton = new Button("<- Fjern");
        removeButton.setOnAction(e -> {
            List<Flag> selected = new ArrayList<>(selectedListView.getSelectionModel().getSelectedItems());
            if (selected.isEmpty()) {
                showError("Ingen flagg valgt i høyre liste.");
                return;
            }
            selectedFlags.removeAll(selected);
            availableFlags.addAll(selected);
        });

        VBox moveButtonsBox = new VBox(10, addButton, removeButton);
        moveButtonsBox.setAlignment(Pos.CENTER);

        // Layout for listene
        HBox listsBox = new HBox(10, availableListView, moveButtonsBox, selectedListView);

        // OK / Avbryt-knapper
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Avbryt");
        HBox bottomButtons = new HBox(10, okButton, cancelButton);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);

        // Sett sammen alt
        VBox mainLayout = new VBox(10, headerLabel, questionsBox, listsBox, bottomButtons);
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);

        // Logikk for OK-knappen
        okButton.setOnAction(e -> {
            int totalQ = quizApp.getTotalQuestions();
            if (selectedFlags.size() != totalQ) {
                showError("Du må velge nøyaktig " + totalQ + " flagg, men har valgt " + selectedFlags.size() + ".");
                return;
            }
            // Alt OK: Oppdater quizApp med antall spørsmål og valgte flagg
            quizApp.setActiveFlags(new ArrayList<>(selectedFlags));
            // Lukk vinduet
            stage.close();
        });

        // Avbryt
        cancelButton.setOnAction(e -> stage.close());

        stage.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
