package com.example.quizapp;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlagManager {
    private List<Flag> flags;
    private final String FILENAME = "flags.ser";

    public FlagManager() {
        try {
            flags = loadFlagsFromFile();
            if (flags == null || flags.isEmpty()) {
                flags = createDefaultFlags();
            }
        } catch (FileOperationException e) {
            e.printStackTrace();
            flags = createDefaultFlags();
        }
    }

    public List<Flag> getFlags() {
        return flags;
    }

    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    public void updateFlag(int index, Flag flag) {
        if (index >= 0 && index < flags.size()) {
            flags.set(index, flag);
        }
    }

    public void deleteFlag(int index) {
        if (index >= 0 && index < flags.size()) {
            flags.remove(index);
        }
    }

    public void saveFlags() throws FileOperationException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILENAME))) {
            out.writeObject(flags);
        } catch (IOException e) {
            throw new FileOperationException("Feil ved lagring av flaggene til fil: " + FILENAME, e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Flag> loadFlagsFromFile() throws FileOperationException {
        File file = new File(FILENAME);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Flag>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new FileOperationException("Feil ved lasting av flaggene fra fil: " + FILENAME, e);
        }
    }

    /**
     * Prøver først å laste flaggene fra CSV-filen "flags.csv" som ligger i src/main/resources.
     * Format per linje: Land;filnavn
     * Bildene skal ligge i /images/ mappen.
     *
     * @return En liste med Flag-objekter basert på CSV-data, eller en tom liste om feilen oppstår.
     */
    private List<Flag> loadFlagsFromCSV() {
        List<Flag> csvFlags = new ArrayList<>();
        InputStream stream = getClass().getResourceAsStream("/flags.csv");
        if (stream == null) {
            System.err.println("flags.csv ble ikke funnet i resources-mappen.");
            return csvFlags;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Hopp over tomme linjer
                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String country = parts[0].trim();
                    String fileName = parts[1].trim();
                    // Bygg den fullstendige stien for bildet
                    String imagePath = "/images/" + fileName;
                    csvFlags.add(new Flag(country, imagePath));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFlags;
    }

    /**
     * Oppretter standardflaggene ved å først forsøke å lese fra CSV.
     * Faller tilbake til en hardkodet liste om CSV ikke er tilgjengelig.
     */
    private List<Flag> createDefaultFlags() {
        List<Flag> csvFlags = loadFlagsFromCSV();
        if (csvFlags != null && !csvFlags.isEmpty()) {
            return csvFlags;
        }
        // Fallback – hardkodet liste
        List<Flag> defaultFlags = new ArrayList<>();
        defaultFlags.add(new Flag("Antigua and Barbuda", "/images/ac-flag.gif"));
        defaultFlags.add(new Flag("Afghanistan", "/images/af-flag.gif"));
        defaultFlags.add(new Flag("Algerie", "/images/ag-flag.gif"));
        defaultFlags.add(new Flag("Albania", "/images/al-flag.gif"));
        defaultFlags.add(new Flag("Armenia", "/images/am-flag.gif"));
        defaultFlags.add(new Flag("Andorra", "/images/an-flag.gif"));
        defaultFlags.add(new Flag("Argentina", "/images/ar-flag.gif"));
        defaultFlags.add(new Flag("Ascension Island", "/images/tn_ac_flag.gif"));
        defaultFlags.add(new Flag("Angola", "/images/tn_ao-flag.gif"));
        defaultFlags.add(new Flag("American Samoa", "/images/as-flag.gif"));
        return defaultFlags;
    }
}
