# Flag Quiz Application

## Description
The **Flag Quiz Application** is an interactive quiz game where users test their knowledge of national flags. The application includes a quiz mode for users and an admin panel for managing flags and quiz settings.

## Installation Instructions
### Prerequisites
- **Java 17+** installed on your system
- **JavaFX** (required for GUI)
- **Maven or Gradle** (if you want to manage dependencies)

### Steps to Run
1. **Clone or Extract the Project**
   ```sh
   git clone https://github.com/martingit2/quiz
   ```
   Or manually extract the ZIP file.

2. **Navigate to the Project Directory**
   ```sh
   cd FlagQuizApp
   ```

3. **Compile and Run the Application**
   ```sh
   javac -cp . --module-path "<path_to_javafx>" --add-modules javafx.controls,javafx.fxml com/example/quizapp/QuizApp.java
   java -cp . --module-path "<path_to_javafx>" --add-modules javafx.controls,javafx.fxml com.example.quizapp.QuizApp
   ```
   *(Make sure to replace `<path_to_javafx>` with the actual path where JavaFX is installed.)*

## Usage Instructions
### Playing the Quiz
1. Start the application.
2. Select the **Start Quiz** option.
3. Identify the correct flag from four multiple-choice options.
4. Scores are saved, and the top five are displayed.

### Admin Panel
1. Click on **Admin Panel** to access the settings.
2. Add, edit, or remove flag entries.
3. Adjust the number of questions per quiz.
4. Save changes, which are stored in `flags.ser`.

## File Structure
```
FlagQuizApp/
├── src/
│   ├── com/example/quizapp/QuizApp.java        # Main application file
│   ├── com/example/quizapp/FlagManager.java    # Handles flag storage and retrieval
│   ├── com/example/quizapp/AdminPanel.java     # Admin panel for managing flags
│   ├── com/example/quizapp/GameConfigPanel.java # Quiz settings management
│   ├── com/example/quizapp/HighScoreManager.java # Stores and retrieves highscores
│   ├── com/example/quizapp/QuizLogger.java     # Logs quiz history
│   ├── com/example/quizapp/Flag.java           # Represents a flag object
│   ├── com/example/quizapp/HighScoreEntry.java # Stores individual scores
├── resources/
│   ├── images/   # Folder containing flag images
│   ├── flags.csv # CSV file with flag data
│   ├── flags.ser # Serialized flag storage
│   ├── highscores.ser # Serialized highscore storage
│   ├── quiz_history.txt # Log of previous quizzes
└── README.md  # This file
```

## Dependencies
- **JavaFX** (For UI rendering)

## Author
- **Martin Pettersen**
- Student ID: **256031**

## Acknowledgments
This project was developed as part of the **OBJ2100 (2025 Vår) Obligatory Assignment 1**.
- AI tools (ChatGPT) were used to **optimize code, debug errors, and suggest improvements**.
- Some parts of the file handling and UI improvements were refined based on AI recommendations.

---
### **Note on Flag Downloader**
The **FlagDownloader** application was developed separately to automate the downloading of flag images. It is **not part of this main application** but was used to collect flag images efficiently from `worldometers.info`. The downloaded images were then stored in the `resources/images/` folder for use in the quiz.

This README provides essential instructions to run and modify the **Flag Quiz Application**. 🚀

