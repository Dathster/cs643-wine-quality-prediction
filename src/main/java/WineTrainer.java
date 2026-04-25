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

public class WineTrainer {
	static Dataset<Row> buildFeatureVector(Dataset<Row> df, String[] exclusions){
		String[] features = df.drop(exclusions).columns();


		VectorAssembler featureVector = new VectorAssembler().setInputCols(features).setOutputCol("features");

		return featureVector.transform(df);

	}

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


    public static void main(String[] args) throws IOException {
		SparkSession spark = SparkSession.builder()
				.appName("WineTrainer")
				.master("local[*]")
				.getOrCreate();

		spark.sparkContext().setLogLevel("ERROR");

		System.out.println("Spark version: " + spark.version());

		// Load the training dataset
		String inputFile = SparkFiles.get("TrainingDataset.csv");
		Dataset<Row> training = prepDataset(spark, inputFile);

		training = training.cache();

		String label = "quality";

		RandomForestClassifier rf = new RandomForestClassifier()
				.setLabelCol(label)
				.setFeaturesCol("features")
				.setNumTrees(150)
				.setMaxDepth(11)
				.setMinInstancesPerNode(5)
				.setWeightCol("weight");

		Dataset<Row> counts = training.groupBy("quality").count();

		Dataset<Row> trainingWithCounts = training.join(counts, "quality");

		long totalCount = training.count();

		Dataset<Row> weightedTraining = trainingWithCounts.withColumn(
				"weight",
				lit((double) totalCount).divide(col("count").cast("double"))
		);

		weightedTraining = weightedTraining.drop("count");

		weightedTraining = weightedTraining.drop("features");

		weightedTraining = buildFeatureVector(weightedTraining, new String[]{"quality", "weight"});

		// Build feature vector

		// Fit the model
		RandomForestClassificationModel wineModel = rf.fit(weightedTraining);

		weightedTraining.show();

		wineModel.write().overwrite().save("wine_model");

		System.out.println("Saved the Random Forest Classification model to file 'wine_model'.");

		spark.stop();
    }
}
