import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DBConnector class handles establishing a database connection to application
public class DBConnector {

    // Database connection link
    private static final String URL = "jdbc:ucanaccess://Database/StudentDB.accdb";

    // Method to create and return a connection to the Access database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Attempts to connect to the database using the file address
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            // Prints stack trace if the connection fails
            e.printStackTrace();
        }
        // Returns the established connection or null if failed
        return conn;
    }
}
