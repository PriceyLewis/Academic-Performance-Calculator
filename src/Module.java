// Module class represents a university module //
public class Module {
    private int ModuleID;               
    private String ModuleName;              
    private int Credits;                     
    private double Grades;                   
    private int YearUndertakingModule;       
    private int Semester;                    
    private int Attendance;                
    private int HoursStudied;                

    // Constructor to initialize a Module object with all required module details //
    public Module(String name, int credits, double grade, int year, int semester, int Attendance, int HoursStudied) {
        this.ModuleName = name;
        this.Credits = credits;
        this.Grades = grade;
        this.YearUndertakingModule = year;
        this.Semester = semester;
        this.Attendance = Attendance;
        this.HoursStudied = HoursStudied;
    }

    // Getters for accessing module properties //
    public String getName() { return ModuleName; }
    public int getCredits() { return Credits; }
    public double getGrade() { return Grades; }
    public int getYearUndertakingModule() { return YearUndertakingModule; }
    public int getSemester() { return Semester; }
    public int getAttendance () { return Attendance; }
    public int getHoursStudied () { return HoursStudied; }
}
