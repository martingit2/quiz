package com.example.quizapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminPanel {
    private FlagManager flagManager;
    private ObservableList<Flag> observableFlags;

    public AdminPanel(FlagManager flagManager) {
        this.flagManager = flagManager;
        observableFlags = FXCollections.observableArrayList(flagManager.getFlags());
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Admin Panel - Rediger spørsmål");

        ListView<Flag> listView = new ListView<>(observableFlags);
        listView.setPrefWidth(300);
        listView.setPrefHeight(400);

        Button addButton = new Button("Legg til");
        Button editButton = new Button("Rediger");
        Button deleteButton = new Button("Slett");
        Button saveButton = new Button("Lagre");
        Button loadButton = new Button("Last inn");
        Button closeButton = new Button("Lukk");

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        HBox fileBox = new HBox(10, saveButton, loadButton, closeButton);
        VBox root = new VBox(10, listView, buttonBox, fileBox);
        root.setPadding(new Insets(10));

        // Legg til flagg
        addButton.setOnAction(e -> {
            Dialog<Flag> dialog = createFlagDialog(null);
            dialog.showAndWait().ifPresent(newFlag -> {
                flagManager.addFlag(newFlag);
                observableFlags.add(newFlag);
            });
        });

        // Rediger flagg
        editButton.setOnAction(e -> {
            Flag selected = listView.getSelectionModel().getSelectedItem();
            int index = listView.getSelectionModel().getSelectedIndex();
            if (selected != null) {
                Dialog<Flag> dialog = createFlagDialog(selected);
                dialog.showAndWait().ifPresent(updatedFlag -> {
                    flagManager.updateFlag(index, updatedFlag);
                    observableFlags.set(index, updatedFlag);
                });
            } else {
                showAlert("Ingen flagg valgt", "Vennligst velg et flagg for å redigere.");
            }
        });

        // Slett flagg
        deleteButton.setOnAction(e -> {
            int index = listView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                flagManager.deleteFlag(index);
                observableFlags.remove(index);
            } else {
                showAlert("Ingen flagg valgt", "Vennligst velg et flagg for å slette.");
            }
        });

        // Lagre flagg til fil med unntakshåndtering
        saveButton.setOnAction(e -> {
            try {
                flagManager.saveFlags();
                showAlert("Lagret", "Flaggene er lagret.");
            } catch (FileOperationException ex) {
                showAlert("Feil", ex.getMessage());
            }
        });

        // Last inn flagg fra fil med unntakshåndtering
        loadButton.setOnAction(e -> {
            try {
                observableFlags.setAll(flagManager.loadFlagsFromFile());
            } catch (FileOperationException ex) {
                showAlert("Feil", ex.getMessage());
            }
        });

        closeButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Dialog for å legge til/redigere et flagg
    private Dialog<Flag> createFlagDialog(Flag flag) {
        Dialog<Flag> dialog = new Dialog<>();
        dialog.setTitle(flag == null ? "Legg til flagg" : "Rediger flagg");

        // Sett opp OK- og Avbryt-knapper
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField countryField = new TextField();
        countryField.setPromptText("Land");
        TextField imagePathField = new TextField();
        imagePathField.setPromptText("Bildebane");

        if (flag != null) {
            countryField.setText(flag.getCountry());
            imagePathField.setText(flag.getImagePath());
        }

        grid.add(new Label("Land:"), 0, 0);
        grid.add(countryField, 1, 0);
        grid.add(new Label("Bildebane:"), 0, 1);
        grid.add(imagePathField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Konverter dialogresultatet til et Flag-objekt ved OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Flag(countryField.getText(), imagePathField.getText());
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
