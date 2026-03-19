import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    private static final String[] DB_PATH_SEGMENTS = { "Database", "StudentDB.accdb" };

    public static Connection getConnection() throws SQLException {
        Path dbPath = resolveProjectPath(DB_PATH_SEGMENTS);
        if (!Files.exists(dbPath)) {
            throw new SQLException("Database file not found: " + dbPath);
        }
        return DriverManager.getConnection("jdbc:ucanaccess://" + dbPath.toAbsolutePath());
    }

    public static Path resolveProjectPath(String... relativeSegments) {
        Path workingDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path candidate = appendSegments(workingDirectory, relativeSegments);
        if (Files.exists(candidate)) {
            return candidate;
        }

        Path parent = workingDirectory.getParent();
        if (parent != null) {
            Path parentCandidate = appendSegments(parent, relativeSegments);
            if (Files.exists(parentCandidate)) {
                return parentCandidate;
            }
        }

        return candidate;
    }

    private static Path appendSegments(Path base, String... relativeSegments) {
        Path current = base;
        for (String segment : relativeSegments) {
            current = current.resolve(segment);
        }
        return current.normalize();
    }
}
