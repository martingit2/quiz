package com.example.quizapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;  // Viktig for layout
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

        // Opprett en ListView for flaggene
        ListView<Flag> listView = new ListView<>(observableFlags);
        listView.setPrefWidth(200);
        listView.setPrefHeight(400);

        // 1) Custom cell factory for å vise et lite flaggikon + landnavn
        listView.setCellFactory(lv -> new ListCell<Flag>() {
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

        // 2) Forhåndsvisning av valgt flagg i større format
        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(150);
        previewImage.setPreserveRatio(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                previewImage.setImage(newVal.getImage());
            } else {
                previewImage.setImage(null);
            }
        });

        // Knapper for administrasjon
        Button addButton = new Button("Legg til");
        Button editButton = new Button("Rediger");
        Button deleteButton = new Button("Slett");
        Button saveButton = new Button("Lagre");
        Button loadButton = new Button("Last inn");
        Button closeButton = new Button("Lukk");

        // Sett knappene i HBox-er
        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        HBox fileBox = new HBox(10, saveButton, loadButton, closeButton);

        // Sett sammen hovedinnhold: ListView til venstre, preview til høyre
        HBox mainContent = new HBox(10, listView, previewImage);
        VBox root = new VBox(10, mainContent, buttonBox, fileBox);
        root.setPadding(new Insets(10));

        // -----------------------------
        // Funksjonalitet for knappene
        // -----------------------------

        // Legg til flagg (åpner dialog)
        addButton.setOnAction(e -> {
            Dialog<Flag> dialog = createFlagDialog(null);
            dialog.showAndWait().ifPresent(newFlag -> {
                flagManager.addFlag(newFlag);
                observableFlags.add(newFlag);
            });
        });

        // Rediger flagg (åpner dialog med forhåndsutfylte data)
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
                previewImage.setImage(null); // Fjern preview hvis valgt flagg slettes
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

        // Last inn flagg fra fil
        loadButton.setOnAction(e -> {
            try {
                List<Flag> loaded = flagManager.loadFlagsFromFile();
                if (loaded != null) {
                    observableFlags.setAll(loaded);
                }
                previewImage.setImage(null); // Nullstill preview
            } catch (FileOperationException ex) {
                showAlert("Feil", ex.getMessage());
            }
        });

        // Lukk vinduet
        closeButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Oppretter en dialog for å legge til/redigere et flagg.
     * Denne dialogen inkluderer en knapp "Velg bildebane" som lar deg velge et bilde fra en liste basert på flags.csv.
     */
    private Dialog<Flag> createFlagDialog(Flag flag) {
        Dialog<Flag> dialog = new Dialog<>();
        dialog.setTitle(flag == null ? "Legg til flagg" : "Rediger flagg");

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

        Label countryLabel = new Label("Land:");
        Label imageLabel = new Label("Bildebane:");
        Button chooseImageButton = new Button("Velg bildebane");

        // Når brukeren trykker på "Velg bildebane" åpnes en dialog som leser fra flags.csv
        chooseImageButton.setOnAction(evt -> {
            String chosenPath = showImageSelectionDialog();
            if (chosenPath != null) {
                imagePathField.setText(chosenPath);
            }
        });

        grid.add(countryLabel, 0, 0);
        grid.add(countryField, 1, 0);
        grid.add(imageLabel, 0, 1);
        grid.add(imagePathField, 1, 1);
        grid.add(chooseImageButton, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Flag(countryField.getText(), imagePathField.getText());
            }
            return null;
        });
        return dialog;
    }

    /**
     * Viser en liten dialog der brukeren kan velge en bildebane fra flags.csv (2. kolonne).
     * Returnerer filbanen (f.eks. "/images/tn_af-flag.gif") eller null om avbrutt.
     */
    private String showImageSelectionDialog() {
        Stage stage = new Stage();
        stage.setTitle("Velg bildebane");

        // Les filnavnene fra flags.csv (kun 2. kolonne)
        List<String> imagePaths = loadImagePathsFromCSV("/flags.csv");

        ObservableList<String> items = FXCollections.observableArrayList(imagePaths);
        ListView<String> listView = new ListView<>(items);
        listView.setCellFactory(lv -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(path);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    if (getClass().getResourceAsStream(path) != null) {
                        imageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(path)));
                    } else {
                        imageView.setImage(null);
                    }
                    setGraphic(imageView);
                }
            }
        });

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Avbryt");
        final String[] chosenPath = new String[1];

        okButton.setOnAction(evt -> {
            chosenPath[0] = listView.getSelectionModel().getSelectedItem();
            stage.close();
        });
        cancelButton.setOnAction(evt -> {
            chosenPath[0] = null;
            stage.close();
        });

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        VBox layout = new VBox(10, listView, buttonBox);
        layout.setPadding(new Insets(10));

        stage.setScene(new Scene(layout, 400, 400));
        stage.showAndWait();

        return chosenPath[0];
    }

    /**
     * Leser flags.csv, henter ut bildefilnavn (andre kolonne) og bygger en liste av stier
     * som "/images/<filnavn>".
     */
    private List<String> loadImagePathsFromCSV(String resourcePath) {
        List<String> imagePaths = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(resourcePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String fileName = parts[1].trim();
                    String path = "/images/" + fileName;
                    imagePaths.add(path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagePaths;
    }

    /**
     * Viser en enkel alert-boks med tittel og melding.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
