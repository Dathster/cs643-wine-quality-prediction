import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionTrainingSummary;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.sql.functions.*;
import org.apache.spark.SparkFiles;

import java.io.IOException;
import java.util.Arrays;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

public class WineRunner {

    static Dataset<Row> prepDataset(SparkSession spark, String filename){
        // Load the dataset
        Dataset<Row> df = spark.read().format("csv")
                .option("header", true)
                .option("inferSchema", true)
                .option("sep", ";")
                .load(filename);

        // Clean up the header column names
        for(String col : df.columns()) {
            String newCol = col.replace("\"", "");
            df = df.withColumnRenamed(col, newCol);
        }

        // Set up feature vector (assuming n-1 cols are features)
        String[] features = Arrays.copyOfRange(df.columns(), 0, df.columns().length-1);
        VectorAssembler featureVector = new VectorAssembler().setInputCols(features).setOutputCol("features");
        df = featureVector.transform(df);

        return df;
    }

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("Input dataset is missing.");
            System.exit(1);
        }

        SparkSession spark = SparkSession.builder()
                .appName("WineTrainer")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("ERROR");

        System.out.println("Spark version: " + spark.version());

        String inputFile = args[0];
        Dataset<Row> validationDataset = prepDataset(spark, inputFile);

        String label = "quality";

        RandomForestClassificationModel rf = RandomForestClassificationModel.load("wine_model");

        Dataset<Row> predictions = rf.transform(validationDataset);

        // Evaluate F1 score
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol(label)
                .setPredictionCol("prediction")
                .setMetricName("f1");

        double f1 = evaluator.evaluate(predictions);

        System.out.println("F1 score for the model based on the given dataset: " + f1);

        spark.stop();



    }
}
