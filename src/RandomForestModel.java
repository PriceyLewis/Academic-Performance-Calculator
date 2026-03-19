
import java.util.*;

public class RandomForestModel {

    // Training data (input features + labels)
    private List<double[]> trainingData = new ArrayList<>();

    // Train the model with the provided data
    public void train(List<double[]> data) {
        trainingData = new ArrayList<>(data);
    }

    // Predict the class (final classification) based on user input
    public String predict(double attendance, double hoursStudied, double grade, double credits) {
        if (trainingData.isEmpty()) {
            return "Unknown";
        }

        // Voting mechanism to make a decision based on training data
        int first = 0, upperSecond = 0, lowerSecond = 0, fail = 0;
        for (double[] entry : trainingData) {
            double a = entry[0]; // Attendance
            double h = entry[1]; // Hours studied
            double g = entry[2]; // Grade
            double c = entry[3]; // Credits
            double label = entry[4]; // Classification label (0 = Fail, 1 = 2:2, 2 = 2:1, 3 = First)

            // Calculate the distance between the input and training data
            double distance = Math.sqrt(
                    Math.pow(a - attendance, 2) +
                    Math.pow(h - hoursStudied, 2) +
                    Math.pow(g - grade, 2) +
                    Math.pow(c - credits, 2)
            );

            if (distance < 20) { // If data is close enough, classify it
                if (label == 3) first++;
                else if (label == 2) upperSecond++;
                else if (label == 1) lowerSecond++;
                else fail++;
            }
        }

        // Majority voting based on the closest neighbors
        if (first >= upperSecond && first >= lowerSecond && first >= fail) return "First Class";
        if (upperSecond >= first && upperSecond >= lowerSecond && upperSecond >= fail) return "Upper Second (2:1)";
        if (lowerSecond >= first && lowerSecond >= upperSecond && lowerSecond >= fail) return "Lower Second (2:2)";
        return "Fail";
    }
}