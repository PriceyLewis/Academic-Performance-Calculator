import java.sql.*;
import java.util.*;

public class ModuleDAO {

    public void addModule(Module m) throws SQLException {
        String sql = "INSERT INTO Modules (ModuleName, Credits, Grade, YearUndertakingModule, Semester, Attendance, HoursStudied) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getName());
            stmt.setInt(2, m.getCredits());
            stmt.setDouble(3, m.getGrade());
            stmt.setInt(4, m.getYearUndertakingModule());
            stmt.setInt(5, m.getSemester());
            stmt.setInt(6, m.getAttendance());
            stmt.setInt(7, m.getHoursStudied());

            stmt.executeUpdate();
        }
    }
    public void deleteModuleByName(String moduleName) throws SQLException {
        String sql = "DELETE FROM Modules WHERE ModuleName=?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, moduleName);
            stmt.executeUpdate();
        }
    }
    public void clearModules() throws SQLException {
        String sql = "DELETE FROM Modules";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
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
                list.add(m);
            }
        } // Connection, Statement, ResultSet automatically closed here!
        
        return list;
    }
}
