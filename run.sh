#!/bin/bash

cd ~/cs643-pa2

mvn compile || exit 1

echo "Compile succeeded"

mvn package || exit 1

echo "Packaging succeeded"

spark-submit  \
--executor-memory 512m \
--driver-memory 512m \
--files TrainingDataset.csv \
--class WineTrainer \
target/wine-quality-spark-pa2-1.0-SNAPSHOT.jar || exit 1

echo "Spark ran"


