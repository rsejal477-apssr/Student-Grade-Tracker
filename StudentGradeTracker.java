import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class StudentGradeTracker extends JFrame {
    private final ArrayList<Student> students = new ArrayList<>();
    private final JTextField nameField = new JTextField(18);
    private final JTextField gradeField = new JTextField(8);
    private final JTextField searchField = new JTextField(15);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Student Name", "Grade", "Result"}, 0) {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel totalValue = new JLabel("0");
    private final JLabel averageValue = new JLabel("—");
    private final JLabel highestValue = new JLabel("—");
    private final JLabel lowestValue = new JLabel("—");

    private static final Color NAVY = new Color(25, 36, 72);
    private static final Color BLUE = new Color(65, 105, 225);
    private static final Color BG = new Color(245, 247, 252);
    private static final Color TEXT = new Color(40, 48, 68);
    // Save data in a clearly defined file inside the project working directory.
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), "StudentGradeTrackerData");
    private static final Path DATA_FILE = DATA_DIR.resolve("student-grades.csv");

    static class Student {
        String name;
        double grade;
        Student(String name, double grade) {
            this.name = name;
            this.grade = grade;
        }
    }

    public StudentGradeTracker() {
        super("Student Grade Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        loadStudents();
        refreshTable(students);
        updateStats();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Student Grade Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel subtitle = new JLabel("Manage student records and monitor academic performance");
        subtitle.setForeground(new Color(210, 219, 242));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        JLabel badge = new JLabel("JAVA • SWING");
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 145, 220)),
                new EmptyBorder(9, 12, 9, 12)));

        header.add(titleBox, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(20, 24, 16, 24));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("TOTAL STUDENTS", totalValue, new Color(65, 105, 225)));
        stats.add(statCard("AVERAGE GRADE", averageValue, new Color(20, 145, 115)));
        stats.add(statCard("HIGHEST GRADE", highestValue, new Color(210, 135, 35)));
        stats.add(statCard("LOWEST GRADE", lowestValue, new Color(150, 90, 190)));

        JPanel lower = new JPanel(new BorderLayout(0, 16));
        lower.setOpaque(false);
        lower.add(buildFormPanel(), BorderLayout.NORTH);
        lower.add(buildTablePanel(), BorderLayout.CENTER);

        content.add(stats, BorderLayout.NORTH);
        content.add(lower, BorderLayout.CENTER);
        return content;
    }

    private JPanel statCard(String heading, JLabel value, Color accent) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(13, 15, 13, 12)));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(heading);
        label.setForeground(new Color(105, 113, 133));
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        value.setForeground(TEXT);
        value.setFont(new Font("SansSerif", Font.BOLD, 25));
        card.add(label);
        card.add(Box.createVerticalStrut(10));
        card.add(value);
        return card;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 230, 240)),
                new EmptyBorder(17, 18, 17, 18)));

        JLabel heading = new JLabel("Student Records");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(TEXT);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        fields.setOpaque(false);
        styleField(nameField);
        styleField(gradeField);
        styleField(searchField);
        fields.add(new JLabel("Name"));
        fields.add(nameField);
        fields.add(new JLabel("Grade (0–100)"));
        fields.add(gradeField);

        JButton addButton = button("Add Student", BLUE);
        JButton updateButton = button("Update Selected", new Color(20, 145, 115));
        JButton deleteButton = button("Delete Selected", new Color(190, 65, 75));
        JButton clearButton = button("Clear", new Color(105, 113, 133));

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearFields());

        fields.add(addButton);
        fields.add(updateButton);
        fields.add(deleteButton);
        fields.add(clearButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.WEST);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        search.setOpaque(false);
        search.add(new JLabel("Search"));
        search.add(searchField);
        JButton searchButton = button("Find", NAVY);
        searchButton.addActionListener(e -> searchStudents());
        searchField.addActionListener(e -> searchStudents());
        search.add(searchButton);
        JButton showAll = button("Show All", new Color(105, 113, 133));
        showAll.addActionListener(e -> refreshTable(students));
        search.add(showAll);
        top.add(search, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(fields, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 230, 240)),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel heading = new JLabel("All Students");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(TEXT);

        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setGridColor(new Color(235, 238, 245));
        table.setSelectionBackground(new Color(220, 231, 255));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(220, 228, 245));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setOpaque(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(198, 216, 255));
        table.setSelectionForeground(new Color(25, 36, 72));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFieldsFromSelection();
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(235, 238, 245)));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 24, 10, 24));
        JLabel hint = new JLabel("Tip: Select a row to edit or delete a student.");
        hint.setForeground(new Color(105, 113, 133));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel status = new JLabel("Student Grade Tracker  |  Java Swing");
        status.setForeground(new Color(105, 113, 133));
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(hint, BorderLayout.WEST);
        footer.add(status, BorderLayout.EAST);
        return footer;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(9, 12, 9, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadStudents() {
        if (!Files.exists(DATA_FILE)) {
            System.out.println("No saved student file yet. It will be created when you add a student.");
            System.out.println("Data file location: " + DATA_FILE.toAbsolutePath());
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int separator = line.lastIndexOf(',');
                if (separator <= 0) continue;
                try {
                    // Names are Base64-encoded so commas and special characters are safe.
                    String name = new String(Base64.getDecoder().decode(line.substring(0, separator)), StandardCharsets.UTF_8);
                    double grade = Double.parseDouble(line.substring(separator + 1));
                    if (!name.trim().isEmpty() && grade >= 0 && grade <= 100) {
                        students.add(new Student(name, grade));
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed records and continue loading valid ones.
                }
            }
            System.out.println("Loaded " + students.size() + " student(s) from: " + DATA_FILE.toAbsolutePath());
        } catch (IOException ex) {
            message("Could not load saved records:\n" + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveStudents() {
        try {
            Files.createDirectories(DATA_DIR);
            try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8)) {
                for (Student student : students) {
                    String encodedName = Base64.getEncoder().encodeToString(student.name.getBytes(StandardCharsets.UTF_8));
                    writer.write(encodedName + "," + student.grade);
                    writer.newLine();
                }
            }
            System.out.println("Saved " + students.size() + " student(s) to: " + DATA_FILE.toAbsolutePath());
        } catch (IOException ex) {
            message("Could not save student records:\n" + ex.getMessage(), "File error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(35, 42, 60));
        field.setCaretColor(new Color(35, 42, 60));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 163, 190), 1),
                new EmptyBorder(7, 9, 7, 9)));
    }

    private void addStudent() {
        String name = nameField.getText().trim();
        Double grade = readGrade();
        if (name.isEmpty()) {
            message("Please enter a student name.", "Missing name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (grade == null) return;
        students.add(new Student(name, grade));
        saveStudents();
        refreshTable(students);
        updateStats();
        clearFields();
        message("Student added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a student row in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String oldName = (String) tableModel.getValueAt(modelRow, 0);
        Student student = findStudentByNameAndGrade(oldName,
                Double.parseDouble(tableModel.getValueAt(modelRow, 1).toString()));
        if (student == null) return;

        String newName = nameField.getText().trim();
        if (newName.isEmpty()) newName = student.name;
        Double newGrade = gradeField.getText().trim().isEmpty()
                ? student.grade : readGrade();
        if (newGrade == null) return;

        student.name = newName;
        student.grade = newGrade;
        saveStudents();
        refreshTable(students);
        updateStats();
        clearFields();
        message("Student record updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a student row in the table first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String name = (String) tableModel.getValueAt(modelRow, 0);
        double grade = Double.parseDouble(tableModel.getValueAt(modelRow, 1).toString());
        Student student = findStudentByNameAndGrade(name, grade);
        if (student != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete " + student.name + "'s record?", "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                students.remove(student);
                saveStudents();
                refreshTable(students);
                updateStats();
                clearFields();
            }
        }
    }

    private void searchStudents() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(students);
            return;
        }
        ArrayList<Student> matches = new ArrayList<>();
        for (Student s : students) {
            if (s.name.toLowerCase().contains(query)) matches.add(s);
        }
        refreshTable(matches);
        if (matches.isEmpty()) message("No matching student found.", "Search", JOptionPane.INFORMATION_MESSAGE);
    }

    private Double readGrade() {
        String text = gradeField.getText().trim();
        try {
            double grade = Double.parseDouble(text);
            if (grade < 0 || grade > 100) {
                message("Grade must be between 0 and 100.", "Invalid grade", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return grade;
        } catch (NumberFormatException ex) {
            message("Enter a valid numeric grade, such as 85 or 92.5.", "Invalid grade", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private Student findStudentByNameAndGrade(String name, double grade) {
        for (Student s : students) {
            if (s.name.equals(name) && Double.compare(s.grade, grade) == 0) return s;
        }
        return null;
    }

    private void refreshTable(ArrayList<Student> records) {
        tableModel.setRowCount(0);
        for (Student s : records) {
            tableModel.addRow(new Object[]{
                    s.name, String.format("%.2f", s.grade),
                    s.grade >= 40 ? "Pass" : "Needs improvement"
            });
        }
    }

    private void updateStats() {
        totalValue.setText(String.valueOf(students.size()));
        if (students.isEmpty()) {
            averageValue.setText("—");
            highestValue.setText("—");
            lowestValue.setText("—");
            return;
        }
        double total = 0;
        double highest = students.get(0).grade;
        double lowest = students.get(0).grade;
        for (Student s : students) {
            total += s.grade;
            highest = Math.max(highest, s.grade);
            lowest = Math.min(lowest, s.grade);
        }
        averageValue.setText(String.format("%.2f", total / students.size()));
        highestValue.setText(String.format("%.2f", highest));
        lowestValue.setText(String.format("%.2f", lowest));
    }

    private void fillFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        nameField.setText(tableModel.getValueAt(modelRow, 0).toString());
        gradeField.setText(tableModel.getValueAt(modelRow, 1).toString());
    }

    private void clearFields() {
        nameField.setText("");
        gradeField.setText("");
        table.clearSelection();
        nameField.requestFocusInWindow();
    }

    private void message(String text, String title, int type) {
        JOptionPane.showMessageDialog(this, text, title, type);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Java's default look and feel.
            }
            new StudentGradeTracker().setVisible(true);
        });
    }
}