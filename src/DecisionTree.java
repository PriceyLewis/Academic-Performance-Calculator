import java.util.ArrayList;
import java.util.Random;

// DecisionTree function with randomised feature weights //
public class DecisionTree {
    
    // Array to store weights for features (attendance, hours studied, semester, credits) //
    private double[] featureWeights;

    // Train method initializes random weights for features //
    public void train(ArrayList<double[]> data) {
        Random rand = new Random();
        featureWeights = new double[4]; 

        // Randomly assigns weights to each feature //
        for (int i = 0; i < featureWeights.length; i++) {
            featureWeights[i] = rand.nextDouble();
        }
    }

    // Predict method calculates a score using weights and classifies it //
    public int predict(double attendance, double hoursStudied, double semester, double credits) {
        // Compute weighted sum of inputs //
        double score = attendance * featureWeights[0] +
                       hoursStudied * featureWeights[1] +
                       semester * featureWeights[2] +
                       credits * featureWeights[3];

        // Classification based on score thresholds //
        if (score > 250) return 3;        // First Class //
        else if (score > 200) return 2;   // Upper Second Class (2:1) //
        else if (score > 150) return 1;   // Lower Second Class (2:2) //
        else return 0;                    // Fail //
    }
}
