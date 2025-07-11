import java.util.*;

// RandomForestModel uses a classification model by utilising voting based on proximity //
public class RandomForestModel {

    // List to store training data samples: [attendance, hours, grade, credits, classification] //
    private List<double[]> trainingData = new ArrayList<>();

    // Method to train the model by saving the training data //
    public void train(List<double[]> data) {
        trainingData = new ArrayList<>(data); // Stores training data //
    }

    // Predicts the academic classification based on user inputs //
    public String predict(double attendance, double hoursStudied, double grade, double credits) {
        // If there’s no training data, prediciton cant be made //
        if (trainingData.isEmpty()) {
            return "Unknown";
        }

        // Counters for each classification label //
        int first = 0, upperSecond = 0, lowerSecond = 0, fail = 0;

        // Loop over each training data entry to determine similarity (distance) //
        for (double[] entry : trainingData) {
            double a = entry[0]; // Attendance //
            double h = entry[1]; // Hours studied //
            double g = entry[2]; // Grade //
            double c = entry[3]; // Credits //
            double label = entry[4]; // Classification label (0 = Fail, 1 = 2:2, 2 = 2:1, 3 = First) //

            // Calculate Euclidean distance between input and training data point //
            double distance = Math.sqrt(
                    Math.pow(a - attendance, 2) +
                    Math.pow(h - hoursStudied, 2) +
                    Math.pow(g - grade, 2) +
                    Math.pow(c - credits, 2)
            );

            // If a training point is close enough, it counts toward its classification //
            if (distance < 20) {
                if (label == 3) first++;
                else if (label == 2) upperSecond++;
                else if (label == 1) lowerSecond++;
                else fail++;
            }
        }

        // Return the classification with the most votes //
        if (first >= upperSecond && first >= lowerSecond && first >= fail) return "First Class";
        if (upperSecond >= first && upperSecond >= lowerSecond && upperSecond >= fail) return "Upper Second (2:1)";
        if (lowerSecond >= first && lowerSecond >= upperSecond && lowerSecond >= fail) return "Lower Second (2:2)";
        return "Fail";
    }
}
