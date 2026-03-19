import java.util.*;

public class RandomForestRegressor {

    private ArrayList<double[]> trainingData;

    public void train(ArrayList<double[]> data) {
        this.trainingData = data;
    }

    public double predict(double attendance, double hoursStudied) {
        //---Simple "regression" Method That Finds Similar Records and Averages User Grades---//
        double totalGrade = 0;
        int count = 0;

        for (double[] record : trainingData) {
            double recAttendance = record[0];
            double recHoursStudied = record[1];
            double recGrade = record[2];

            //---If similar enough---// 
            if (Math.abs(recAttendance - attendance) <= 10 && Math.abs(recHoursStudied - hoursStudied) <= 20) {
                totalGrade += recGrade;
                count++;
            }
        }

        if (count == 0) {
            return 50.0; // Defaults if no match found
        }

        return totalGrade / count;
    }
}
