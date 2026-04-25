#!/bin/bash

# Ensure the input file is provided
if [ $# -lt 1 ]; then
	echo "Input dataset was not provided, please pass it in as an argument"
	exit 1
fi

# Load the filename
fname=$1

mvn compile || exit 1

echo "Compile succeeded"

mvn package || exit 1

echo "Packaging succeeded"

spark-submit  \
--executor-memory 512m \
--driver-memory 512m \
--files TrainingDataset.csv \
--class WineRunner \
target/wine-quality-spark-pa2-1.0-SNAPSHOT.jar $fname || exit 1

echo "Spark ran"


