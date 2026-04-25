#!/bin/bash

mvn compile || exit 1

echo "Compile succeeded"

mvn package || exit 1

echo "Packaging succeeded"

spark-submit  \
--executor-memory 512m \
--driver-memory 512m \
# --master <MASTER_SPARK_IP_ADDRESS>
--files TrainingDataset.csv \
--class WineTrainer \
target/wine-quality-spark-pa2-1.0-SNAPSHOT.jar || exit 1

echo "Spark ran"


