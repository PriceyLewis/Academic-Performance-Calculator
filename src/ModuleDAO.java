import java.sql.*;
import java.util.*;

// ModuleDAO provides database functions for Module objects //
public class ModuleDAO {

    // Adds a Module to the database //
    public void addModule(Module m) throws SQLException {
        String sql = "INSERT INTO Modules (ModuleName, Credits, Grade, YearUndertakingModule, Semester, Attendance, HoursStudied) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Sets values from the Module object //
            stmt.setString(1, m.getName());
            stmt.setInt(2, m.getCredits());
            stmt.setDouble(3, m.getGrade());
            stmt.setInt(4, m.getYearUndertakingModule());
            stmt.setInt(5, m.getSemester());
            stmt.setInt(6, m.getAttendance());
            stmt.setInt(7, m.getHoursStudied());

            stmt.executeUpdate(); // Executes insert //
        }
    }

    // Deletes a module by name from the database //
    public void deleteModuleByName(String moduleName) throws SQLException {
        String sql = "DELETE FROM Modules WHERE ModuleName=?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, moduleName); // Set module name
            stmt.executeUpdate();          // Execute delete
        }
    }

    // Clears all module records from the database //
    public void clearModules() throws SQLException {
        String sql = "DELETE FROM Modules";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    // Retrieves all modules from the database and returns them as a list //
    public List<Module> getAllModules() throws SQLException {
        List<Module> list = new ArrayList<>();
        String sql = "SELECT * FROM Modules";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Module m = new Module(
                    rs.getString("ModuleName"),
                    rs.getInt("Credits"),
                    rs.getDouble("Grade"),
                    rs.getInt("YearUndertakingModule"),
                    rs.getInt("Semester"),
                    rs.getInt("Attendance"),
                    rs.getInt("HoursStudied")
                );
                list.add(m); // Add retrieved module to the list //
            }
        }

        return list; // Return all retrieved modules //
    }

    // Inserts hardcoded sample module data into the database //
    public void insertSampleData() throws SQLException {
        List<Module> sampleModules = Arrays.asList(
            new Module("Maths", 20, 75.5, 2024, 1, 90, 40),
            new Module("Java Programming", 20, 80.0, 2024, 1, 85, 35),
            new Module("AI Fundamentals", 20, 78.0, 2024, 2, 88, 38)
        );

        for (Module m : sampleModules) {
            addModule(m); // Add each sample module to the database //
        }
    }
}
