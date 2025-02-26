package com.example.quizapp;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.io.Serializable;

public class Flag implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String country;
    private final String imagePath;

    public Flag(String country, String imagePath) {
        this.country = country;
        this.imagePath = imagePath;
    }

    public String getCountry() {
        return country;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Image getImage() {
        InputStream stream = getClass().getResourceAsStream(imagePath);
        if (stream == null) {
            System.err.println("Bilde ikke funnet: " + imagePath);
            // Prøv å returnere et standard placeholder-bilde
            InputStream placeholderStream = getClass().getResourceAsStream("/images/placeholder.png");
            if (placeholderStream != null) {
                return new Image(placeholderStream);
            } else {
                // Returner et tomt bilde (1x1 transparent PNG) som fallback
                return new Image("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
            }
        }
        return new Image(stream);
    }

    @Override
    public String toString() {
        return country;
    }
}
