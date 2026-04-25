#!/bin/bash

# Ensure the input file is provided
if [ $# -lt 1 ]; then
	echo "Input dataset was not provided, please pass it in as an argument"
	exit 1
fi

# Load the filename
fname=$1

# Download the docker container
docker -v || { 
	echo "Docker is not installed in your system, please ensure to install it before running this script" 
	exit 1 
}

docker pull dathster/cs643-wine-predictor:latest

# Run the docker container
docker run --rm -v $(pwd):/data dathster/cs643-wine-predictor:latest /data/$fname
