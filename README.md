# Student Grade Tracker

A desktop-based Student Grade Tracker built with Java Swing. The application helps users manage student records, update grades, search for students, and view grade statistics through a graphical interface.

## Features

- Add student records with a name and grade.
- View student names, grades, and a simple result status in a table.
- Search for students by name.
- Update a selected student's name or grade.
- Delete records with a confirmation prompt.
- Calculate total students, average grade, highest grade, and lowest grade.
- Validate grades to ensure they are between 0 and 100.
- Save records to a local CSV file and load them when the application starts.

## Technologies Used

- **Java**
- **Java Swing** for the graphical user interface
- **ArrayList** for managing student records in memory
- **File I/O** for saving and loading records
- **CSV** for local data storage

## Requirements

- JDK installed (JDK 8 or later is suitable for the code's core APIs).
- IntelliJ IDEA, Eclipse, or another Java IDE (optional if compiling from the terminal).

## Run the Project

1. Clone or download this repository.
2. Open the project in your Java IDE.
3. Put `StudentGradeTracker.java` in your source folder.
4. Run the `main()` method in `StudentGradeTracker`.

You can also compile and run it from a terminal in the folder containing the file:

```bash
javac StudentGradeTracker.java
java StudentGradeTracker
```

## Data Storage

The application saves student records to a local file named `student-grades.csv`. The exact location depends on the application's configured data-file path. Keep this file if you want to preserve your saved student records when moving or backing up the project.

**Privacy note:** The CSV file may contain student names and grades. Do not commit real or sensitive student data to a public repository. If your CSV contains personal data, keep it out of GitHub.

## Project Structure

```text
Student-Grade-Tracker/
├── StudentGradeTracker.java
├── README.md
└── student-grades.csv   # Local data file; do not upload real student data
```

## Future Improvements

- Add student IDs to distinguish students with the same name.
- Export reports to CSV or PDF.
- Add sorting by name or grade.
- Add database support (for example, MySQL or SQLite).
- Improve the interface with charts and additional grade summaries.

## Author

**Sejal Rai**

---

This project was created for learning and practicing Java programming, object-oriented programming, GUI development, collections, and file handling.
