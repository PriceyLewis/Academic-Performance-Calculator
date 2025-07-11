import java.util.*;

// RandomForestRegressor uses a simple regression model to predict grades //
public class RandomForestRegressor {

    // Stores the training dataset: [attendance, hours studied, grade] //
    private ArrayList<double[]> trainingData;

    // Saves training data into the model //
    public void train(ArrayList<double[]> data) {
        this.trainingData = data;
    }

    // Predicts grade based on attendance and hours studied //
    public double predict(double attendance, double hoursStudied) {
        // regression that averages grades of similar students //
        double totalGrade = 0;
        int count = 0;

        // Loop through training data to find similar cases //
        for (double[] record : trainingData) {
            double recAttendance = record[0];
            double recHoursStudied = record[1];
            double recGrade = record[2];

            // Checks for similarity //
            if (Math.abs(recAttendance - attendance) <= 10 &&
                Math.abs(recHoursStudied - hoursStudied) <= 20) {
                totalGrade += recGrade;
                count++;
            }
        }

        // If no similar cases are found it returns a default average //
        if (count == 0) {
            return 50.0; // Default prediction when no match is found //
        }

        // Return average grade of similar training samples //
        return totalGrade / count;
    }
}
