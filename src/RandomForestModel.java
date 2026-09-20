import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Small educational random-forest classifier used by the portfolio demo.
 *
 * Each tree is trained on a bootstrap sample. At every split the tree considers
 * a random subset of the four input features and selects the threshold with the
 * best Gini-impurity reduction. The forest prediction is the majority vote
 * across all trees.
 *
 * This is intentionally dependency-free so the implementation can be inspected
 * during interviews. It is a portfolio demonstration, not a validated academic
 * outcome model.
 */
public class RandomForestModel {
    private static final int FEATURE_COUNT = 4;
    private static final int CLASS_COUNT = 4;
    private static final int DEFAULT_TREE_COUNT = 101;
    private static final int DEFAULT_MAX_DEPTH = 6;
    private static final int DEFAULT_MIN_SAMPLES_SPLIT = 2;
    private static final int DEFAULT_FEATURES_PER_SPLIT = 2;
    private static final long DEFAULT_SEED = 20260919L;

    private final int treeCount;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int featuresPerSplit;
    private final long seed;
    private final List<Tree> trees = new ArrayList<>();

    public RandomForestModel() {
        this(DEFAULT_TREE_COUNT, DEFAULT_MAX_DEPTH, DEFAULT_MIN_SAMPLES_SPLIT,
            DEFAULT_FEATURES_PER_SPLIT, DEFAULT_SEED);
    }

    RandomForestModel(int treeCount, int maxDepth, int minSamplesSplit,
                      int featuresPerSplit, long seed) {
        if (treeCount < 1) {
            throw new IllegalArgumentException("treeCount must be positive");
        }
        this.treeCount = treeCount;
        this.maxDepth = Math.max(1, maxDepth);
        this.minSamplesSplit = Math.max(2, minSamplesSplit);
        this.featuresPerSplit = Math.max(1, Math.min(FEATURE_COUNT, featuresPerSplit));
        this.seed = seed;
    }

    public void train(List<double[]> rawData) {
        List<double[]> data = sanitise(rawData);
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be empty.");
        }

        trees.clear();
        Random forestRandom = new Random(seed);
        for (int i = 0; i < treeCount; i++) {
            List<double[]> bootstrap = bootstrapSample(data, forestRandom);
            Tree tree = new Tree(maxDepth, minSamplesSplit, featuresPerSplit,
                new Random(forestRandom.nextLong()));
            tree.train(bootstrap);
            trees.add(tree);
        }
    }

    public String predict(double attendance, double hoursStudied, double grade, double credits) {
        return predictWithConfidence(attendance, hoursStudied, grade, credits).classification();
    }

    public Prediction predictWithConfidence(double attendance, double hoursStudied,
                                            double grade, double credits) {
        if (trees.isEmpty()) {
            return new Prediction("Unknown", 0, new int[CLASS_COUNT]);
        }

        double[] features = {attendance, hoursStudied, grade, credits};
        int[] votes = new int[CLASS_COUNT];
        for (Tree tree : trees) {
            votes[tree.predict(features)]++;
        }

        int winningClass = 0;
        for (int i = 1; i < votes.length; i++) {
            if (votes[i] > votes[winningClass]) {
                winningClass = i;
            }
        }

        int confidence = (int) Math.round((votes[winningClass] * 100.0) / trees.size());
        return new Prediction(className(winningClass), confidence, votes.clone());
    }

    public int getTreeCount() {
        return trees.size();
    }

    public record Prediction(String classification, int confidencePercent, int[] votes) {
        @Override
        public int[] votes() {
            return votes.clone();
        }
    }

    private List<double[]> sanitise(List<double[]> rawData) {
        if (rawData == null) {
            return Collections.emptyList();
        }

        List<double[]> cleaned = new ArrayList<>();
        for (double[] row : rawData) {
            if (row == null || row.length < FEATURE_COUNT + 1) {
                continue;
            }
            boolean finite = true;
            for (int i = 0; i < FEATURE_COUNT; i++) {
                finite &= Double.isFinite(row[i]);
            }
            double rawLabel = row[FEATURE_COUNT];
            int label = (int) Math.round(rawLabel);
            if (finite && Double.isFinite(rawLabel) && label >= 0 && label < CLASS_COUNT) {
                cleaned.add(Arrays.copyOf(row, FEATURE_COUNT + 1));
            }
        }
        return cleaned;
    }

    private List<double[]> bootstrapSample(List<double[]> data, Random random) {
        List<double[]> sample = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            sample.add(data.get(random.nextInt(data.size())));
        }
        return sample;
    }

    private static String className(int label) {
        return switch (label) {
            case 3 -> "First Class";
            case 2 -> "Upper Second (2:1)";
            case 1 -> "Lower Second (2:2)";
            default -> "Fail";
        };
    }

    private static final class Tree {
        private final int maxDepth;
        private final int minSamplesSplit;
        private final int featuresPerSplit;
        private final Random random;
        private Node root;

        Tree(int maxDepth, int minSamplesSplit, int featuresPerSplit, Random random) {
            this.maxDepth = maxDepth;
            this.minSamplesSplit = minSamplesSplit;
            this.featuresPerSplit = featuresPerSplit;
            this.random = random;
        }

        void train(List<double[]> rows) {
            root = build(rows, 0);
        }

        int predict(double[] features) {
            Node node = root;
            while (!node.leaf) {
                node = features[node.featureIndex] <= node.threshold ? node.left : node.right;
            }
            return node.predictedClass;
        }

        private Node build(List<double[]> rows, int depth) {
            int majority = majorityClass(rows);
            if (rows.isEmpty() || depth >= maxDepth || rows.size() < minSamplesSplit || isPure(rows)) {
                return Node.leaf(majority);
            }

            Split split = findBestSplit(rows);
            if (split == null || split.gain <= 1e-12) {
                return Node.leaf(majority);
            }

            List<double[]> left = new ArrayList<>();
            List<double[]> right = new ArrayList<>();
            for (double[] row : rows) {
                if (row[split.featureIndex] <= split.threshold) {
                    left.add(row);
                } else {
                    right.add(row);
                }
            }

            if (left.isEmpty() || right.isEmpty()) {
                return Node.leaf(majority);
            }

            return Node.branch(
                split.featureIndex,
                split.threshold,
                build(left, depth + 1),
                build(right, depth + 1),
                majority
            );
        }

        private Split findBestSplit(List<double[]> rows) {
            double parentImpurity = gini(rows);
            Split best = null;
            for (int feature : randomFeatureSubset()) {
                List<Double> values = rows.stream()
                    .map(row -> row[feature])
                    .distinct()
                    .sorted(Comparator.naturalOrder())
                    .toList();

                for (int i = 1; i < values.size(); i++) {
                    double threshold = (values.get(i - 1) + values.get(i)) / 2.0;
                    int leftCount = 0;
                    int rightCount = 0;
                    int[] leftClasses = new int[CLASS_COUNT];
                    int[] rightClasses = new int[CLASS_COUNT];

                    for (double[] row : rows) {
                        int label = (int) Math.round(row[FEATURE_COUNT]);
                        if (row[feature] <= threshold) {
                            leftCount++;
                            leftClasses[label]++;
                        } else {
                            rightCount++;
                            rightClasses[label]++;
                        }
                    }

                    if (leftCount == 0 || rightCount == 0) {
                        continue;
                    }

                    double weighted = (leftCount * gini(leftClasses, leftCount)
                        + rightCount * gini(rightClasses, rightCount)) / rows.size();
                    double gain = parentImpurity - weighted;
                    if (best == null || gain > best.gain) {
                        best = new Split(feature, threshold, gain);
                    }
                }
            }
            return best;
        }

        private int[] randomFeatureSubset() {
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < FEATURE_COUNT; i++) {
                all.add(i);
            }
            Collections.shuffle(all, random);
            return all.subList(0, featuresPerSplit).stream().mapToInt(Integer::intValue).toArray();
        }

        private boolean isPure(List<double[]> rows) {
            int label = (int) Math.round(rows.get(0)[FEATURE_COUNT]);
            for (int i = 1; i < rows.size(); i++) {
                if ((int) Math.round(rows.get(i)[FEATURE_COUNT]) != label) {
                    return false;
                }
            }
            return true;
        }

        private int majorityClass(List<double[]> rows) {
            if (rows.isEmpty()) {
                return 0;
            }
            int[] counts = new int[CLASS_COUNT];
            for (double[] row : rows) {
                counts[(int) Math.round(row[FEATURE_COUNT])]++;
            }
            int best = 0;
            for (int i = 1; i < counts.length; i++) {
                if (counts[i] > counts[best]) {
                    best = i;
                }
            }
            return best;
        }

        private double gini(List<double[]> rows) {
            int[] counts = new int[CLASS_COUNT];
            for (double[] row : rows) {
                counts[(int) Math.round(row[FEATURE_COUNT])]++;
            }
            return gini(counts, rows.size());
        }

        private double gini(int[] counts, int total) {
            if (total == 0) {
                return 0.0;
            }
            double sumSquares = 0.0;
            for (int count : counts) {
                double p = count / (double) total;
                sumSquares += p * p;
            }
            return 1.0 - sumSquares;
        }
    }

    private record Split(int featureIndex, double threshold, double gain) {
    }

    private static final class Node {
        final boolean leaf;
        final int predictedClass;
        final int featureIndex;
        final double threshold;
        final Node left;
        final Node right;

        private Node(boolean leaf, int predictedClass, int featureIndex,
                     double threshold, Node left, Node right) {
            this.leaf = leaf;
            this.predictedClass = predictedClass;
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.left = left;
            this.right = right;
        }

        static Node leaf(int predictedClass) {
            return new Node(true, predictedClass, -1, Double.NaN, null, null);
        }

        static Node branch(int featureIndex, double threshold, Node left, Node right,
                           int fallbackClass) {
            if (left == null || right == null) {
                return leaf(fallbackClass);
            }
            return new Node(false, fallbackClass, featureIndex, threshold, left, right);
        }
    }
}
