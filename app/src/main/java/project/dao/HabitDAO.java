package project.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.model.Habit;

public class HabitDAO {
    private static final Logger logger = LogManager.getLogger(HabitDAO.class);
    private final String DB_URL = "jdbc:sqlite:habit_tracker.db"; // SQLite Database URL
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


    public HabitDAO() {
        createTable(); // Ensure table exists
    }

    private void createTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS habits (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "name TEXT NOT NULL, " +
                         "completed INTEGER NOT NULL, " +
                         "created_date TEXT NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.error("Error creating table: " + e.getMessage(), e);
        }
    }

    // ✅ Load all habits from database
    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT id, name, completed, created_date FROM habits";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                habits.add(new Habit(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("completed") == 1,
                    LocalDate.parse(rs.getString("created_date"))
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving habits: " + e.getMessage(), e);
        }
        return habits;
    }

    // ✅ Add a new habit
    public void addHabit(Habit habit) {
        String sql = "INSERT INTO habits (name, completed, created_date) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setInt(2, habit.isCompleted() ? 1 : 0);
            pstmt.setString(3, habit.getCreatedDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding habit: " + e.getMessage(), e);
        }
    }

    // ✅ Update habit
    public void updateHabit(Habit habit) {
        String sql = "UPDATE habits SET name = ?, completed = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setInt(2, habit.isCompleted() ? 1 : 0);
            pstmt.setInt(3, habit.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating habit: " + e.getMessage(), e);
        }
    }
    public void deleteHabit(Habit habit) {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, habit.getId());
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Habit deleted: " + habit.getName());
            } else {
                logger.warn("No habit found with ID: " + habit.getId());
            }
        } catch (SQLException e) {
            logger.error("Error deleting habit: " + e.getMessage(), e);
        }
    }
    public int getCompletedHabitsCount() {
        String sql = "SELECT COUNT(*) FROM habits WHERE completed = 1"; // Ensure correct query
    
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            logger.error("Error counting completed habits: " + e.getMessage(), e);
        }
        return 0;
    }
}
