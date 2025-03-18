package project;

import java.awt.HeadlessException;
import java.time.LocalDate;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import project.model.Habit;
import project.ui.MainFrame;

public class App {

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf()); // Apply modern theme
            } catch (UnsupportedLookAndFeelException | HeadlessException e) {
                // Prints full stack trace for better debugging 
                e.printStackTrace();               
            }

            @SuppressWarnings("unused")
            Habit myHabit = new Habit("Exercise", LocalDate.now());

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
