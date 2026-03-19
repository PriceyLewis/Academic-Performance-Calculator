import java.util.ArrayList;
import java.util.Random;

public class DecisionTree {
    private double[] featureWeights;

    public void train(ArrayList<double[]> data) {
        Random rand = new Random();
        featureWeights = new double[4];
        for (int i = 0; i < featureWeights.length; i++) {
            featureWeights[i] = rand.nextDouble();
        }
    }

    public int predict(double attendance, double hoursStudied, double semester, double credits) {
        double score = attendance * featureWeights[0] +
                       hoursStudied * featureWeights[1] +
                       semester * featureWeights[2] +
                       credits * featureWeights[3];
        if (score > 250) return 3; // First
        else if (score > 200) return 2; // 2:1
        else if (score > 150) return 1; // 2:2
        else return 0; // Fail
    }
}