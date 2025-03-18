package project.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import project.dao.HabitDAO;
import project.model.Habit;
import project.services.GoogleCalendarSyncService;

public class MainFrame extends JFrame {

    private final DefaultListModel<Habit> habitListModel;
    private final JList<Habit> habitList;
    private final JButton addButton, editButton, generateReportButton,syncCalendarButton,deleteButton;
    private final HabitDAO habitDAO;
    private final GoogleCalendarSyncService calendarSyncService;


    @SuppressWarnings({ "Convert2Lambda", "unused" })
    public MainFrame() {
        super("Habit Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLayout(new BorderLayout());

        // Load FlatLaf theme
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }

        // Initialize DAO
        habitDAO = new HabitDAO();
        calendarSyncService = new GoogleCalendarSyncService();


        // Habit list model and UI setup
        habitListModel = new DefaultListModel<>();
        habitList = new JList<>(habitListModel);
        habitList.setCellRenderer(new HabitListRenderer());
        JScrollPane scrollPane = new JScrollPane(habitList);

        // Enable list selection to update completion status
        habitList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Habit selectedHabit = habitList.getSelectedValue();
                    if (selectedHabit != null) {
                        selectedHabit.setCompleted(!selectedHabit.isCompleted());
                        habitDAO.updateHabit(selectedHabit); // Update in DB
                        habitList.repaint(); // Refresh UI
                    }
                }
            }
        });

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        addButton = new JButton("Add Habit");
        editButton = new JButton("Edit Habit");
        generateReportButton = new JButton("Generate Monthly Report");
        syncCalendarButton = new JButton("Sync with Google Calendar");
        deleteButton = new JButton("Delete Habit");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(generateReportButton);
        buttonPanel.add(syncCalendarButton);
        buttonPanel.add(deleteButton);

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load habits from DB
        loadHabits();

        // Button Listeners
        addButton.addActionListener(e -> addHabit());
        editButton.addActionListener(e -> editHabit());
        generateReportButton.addActionListener(e -> generateMonthlyReport());
        syncCalendarButton.addActionListener(e -> syncWithGoogleCalendar());
        deleteButton.addActionListener(e -> deleteHabit());

        setVisible(true);
    }

    public MainFrame(JButton addButton, GoogleCalendarSyncService calendarSyncService, JButton editButton, JButton generateReportButton, HabitDAO habitDAO, JList<Habit> habitList, DefaultListModel<Habit> habitListModel, JButton syncCalendarButton,JButton deleteButton, GraphicsConfiguration gc) {
        super(gc);
        this.addButton = addButton;
        this.calendarSyncService = calendarSyncService;
        this.editButton = editButton;
        this.generateReportButton = generateReportButton;
        this.habitDAO = habitDAO;
        this.habitList = habitList;
        this.habitListModel = habitListModel;
        this.syncCalendarButton = syncCalendarButton;
        this.deleteButton = deleteButton; 
    }

    private void syncWithGoogleCalendar() {
        try {
            calendarSyncService.syncCompletedHabits(); // ✅ Call on instance
            JOptionPane.showMessageDialog(this, "Habits successfully synced with Google Calendar!");
        } catch (HeadlessException e) {
            JOptionPane.showMessageDialog(this, "Failed to sync habits: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHabits() {
        habitListModel.clear();
        List<Habit> habits = habitDAO.getAllHabits();
        for (Habit habit : habits) {
            habitListModel.addElement(habit);
        }
    }

    private void addHabit() {
        String habitName = JOptionPane.showInputDialog(this, "Enter Habit Name:");
        if (habitName != null && !habitName.trim().isEmpty()) {
            Habit newHabit = new Habit(habitName, LocalDate.now());
            habitDAO.addHabit(newHabit);
            habitListModel.addElement(newHabit);
        }
    }

    private void editHabit() {
        Habit selectedHabit = habitList.getSelectedValue();
        if (selectedHabit != null) {
            String newName = JOptionPane.showInputDialog(this, "Edit Habit Name:", selectedHabit.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                selectedHabit.setName(newName);
                habitDAO.updateHabit(selectedHabit);
                habitList.repaint();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a habit to edit.");
        }
    }

    private void deleteHabit() {
        Habit selectedHabit = habitList.getSelectedValue();
        if (selectedHabit != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this habit?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                habitDAO.deleteHabit(selectedHabit); // Remove from database
                habitListModel.removeElement(selectedHabit); // Remove from UI list
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a habit to delete.");
        }
    }


    private void generateMonthlyReport() {
        List<Habit> habits = habitDAO.getAllHabits();
        int totalHabits = habits.size();
        int completedCount = 0;

        for (Habit habit : habits) {
            if (habit.isCompleted()) {
                completedCount++;
            }
        }

        double completionRate = (totalHabits == 0) ? 0 : (completedCount * 100.0 / totalHabits);
        String advice = completionRate > 70 ? "Great job! Keep up the good habits." :
                        completionRate > 40 ? "You're doing okay. Try to improve consistency." :
                        "You need to be more consistent with your habits.";

        JOptionPane.showMessageDialog(this, """
                                            Monthly Report:
                                            Total Habits: """ + totalHabits + "\n" +
                "Completed: " + completedCount + "\n" +
                "Completion Rate: " + String.format("%.2f", completionRate) + "%\n\n" +
                "Advice: " + advice);
    }



    // Custom Renderer to display habits with checkboxes
    private class HabitListRenderer extends JCheckBox implements ListCellRenderer<Habit> {
        @SuppressWarnings("unused")
        @Override
        public Component getListCellRendererComponent(JList<? extends Habit> list, Habit habit, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(habit.getName());
            setSelected(habit.isCompleted());

            // Toggle completion status when checkbox is clicked
            addActionListener(e -> {
                habit.setCompleted(!habit.isCompleted());
                habitDAO.updateHabit(habit); // Update database
                habitList.repaint();
            });

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}
