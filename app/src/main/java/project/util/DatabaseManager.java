package project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.Logger;

public class DatabaseManager {

    private static final Logger logger = LoggerUtil.getLogger(DatabaseManager.class);
    private static final String URL = "jdbc:sqlite:habit_tracker.db";

    // Create a single-threaded executor to handle DB tasks
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void connectAsync() {
        executor.submit(() -> {
            try (Connection conn = DriverManager.getConnection(URL)) {
                if (conn != null) {
                    logger.info("Connected to SQLite database successfully.");
                }
            } catch (SQLException e) {
                logger.error("Database connection failed: " + e.getMessage());
            }
        });
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
