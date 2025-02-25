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
            // Her kan du eventuelt returnere et standardbilde
            return new Image(getClass().getResourceAsStream("/images/placeholder.png"));
        }
        return new Image(stream);
    }

    @Override
    public String toString() {
        return country;
    }
}
