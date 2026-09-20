import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RandomForestModelTest {
    private List<double[]> trainingData() {
        List<double[]> rows = new ArrayList<>();
        for (int grade = 30; grade <= 88; grade += 2) {
            int label = grade >= 70 ? 3 : grade >= 60 ? 2 : grade >= 50 ? 1 : 0;
            double attendance = Math.min(98, 50 + grade * 0.5);
            double studyHours = 35 + grade * 1.8;
            double credits = grade % 3 == 0 ? 30 : 15;
            rows.add(new double[] { attendance, studyHours, grade, credits, label });
        }
        return rows;
    }

    @Test
    void buildsARealForestAndClassifiesRepresentativeGrades() {
        RandomForestModel model = new RandomForestModel();
        model.train(trainingData());

        assertEquals(101, model.getTreeCount());
        assertEquals("Fail", model.predict(65, 85, 42, 15));
        assertEquals("Lower Second (2:2)", model.predict(76, 130, 55, 15));
        assertEquals("Upper Second (2:1)", model.predict(82, 150, 65, 30));
        assertEquals("First Class", model.predict(91, 180, 78, 15));
    }

    @Test
    void rejectsTrainingSetsWithOnlyInvalidLabels() {
        RandomForestModel model = new RandomForestModel();
        List<double[]> invalid = new ArrayList<>();
        invalid.add(new double[] { 80, 120, 65, 15, Double.NaN });
        invalid.add(new double[] { 80, 120, 65, 15, Double.POSITIVE_INFINITY });

        assertThrows(IllegalArgumentException.class, () -> model.train(invalid));
    }

    @Test
    void untrainedModelReturnsUnknownInsteadOfCrashing() {
        RandomForestModel model = new RandomForestModel();
        RandomForestModel.Prediction result = model.predictWithConfidence(80, 120, 65, 15);

        assertEquals("Unknown", result.classification());
        assertEquals(0, result.confidencePercent());
        assertEquals(0, java.util.Arrays.stream(result.votes()).sum());
    }

    @Test
    void reportsMajorityVoteConfidence() {
        RandomForestModel model = new RandomForestModel();
        model.train(trainingData());

        RandomForestModel.Prediction prediction =
            model.predictWithConfidence(90, 180, 78, 15);

        assertEquals("First Class", prediction.classification());
        assertTrue(prediction.confidencePercent() >= 50);
        assertEquals(101, java.util.Arrays.stream(prediction.votes()).sum());
    }
}
