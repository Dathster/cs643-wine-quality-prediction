# cs643-wine-quality-prediction
Programming Assignment 2 where I built a Spark application to train a machine learning model across 4 EC2 instances and run a prediction application on Docker image

### Github Repo Link:
https://github.com/Dathster/cs643-wine-quality-prediction

### Docker Hub Repo Link:
https://hub.docker.com/repository/docker/dathster/cs643-wine-predictor/general

## Cloud environment setup for parallel training

### EC2 Creation

- Go to AWS → EC2 → Launch Instance
- Choose Ubuntu 24.04 for the Operating System
- Choose t3.small for the instance type
- Generate/use an exisisting keypair to access the VMs
- For network settings, choose create security group, press edit and configure the following rules:
    - Custom TCP, port range 7077, Source Custom, but under prefix lists, choose the current security group (or 0.0.0.0/0)
    - Custom TCP, port range 8080 Source Custom, but under prefix lists, choose the current security group (or 0.0.0.0/0)
    - Custom TCP, port range 8081, Source Custom, but under prefix lists, choose the current security group (or 0.0.0.0/0)
    - SSH, (leave default SSH config as is)
- Leave storage config as is (8GiB gp3)
- Set number of instances to 4
- Launch the instances

### EC2 setup

- SSH into each of the instances and run the following commands to install the required software
    
    ```bash
    sudo apt update
    
    # Installing JDK 
    sudo apt install -y openjdk-17-jdk
    
    # Verify if JDK installation was successful
    java -version
    
    # Installing Maven
    sudo apt install -y maven
    
    # Verifying Maven installation was successful
    mvn -v 
    
    # Installing Spark
    wget https://downloads.apache.org/spark/spark-3.5.8/spark-3.5.8-bin-hadoop3.tgz
    tar -xzf spark-3.5.8-bin-hadoop3.tgz
    mv spark-3.5.8-bin-hadoop3 spark
    
    # Updating ~/.bashrc with env variables for Spark
    echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
    echo 'export SPARK_HOME=~/spark' >> ~/.bashrc
    echo 'export $PATH:$SPARK_HOME/bin:$PATH'
    
    source ~/.bashrc
    
    # Verify Spark installation was successful
    spark-shell 
    ```
    

### Running the Spark Job

- Do the following steps in the master node
    - Run `start-master.sh` to set up the Master node in the Spark cluster.
    - Run `grep -i "spark://" ~/spark/logs/*Master*.out` to get the IP address for the master node. It will be in the following format: spark://172.31.46.195:7077
- Do the following steps in the 3 other worker nodes
    - Run `start-worker.sh <master IP address>`
- Run the following steps in all 4 EC2 instances
    - Ensure you copied the `cs643-pa2` directory containing all the project files into the ~/ folder and then cd into it.
    - In the folder, use `nano [run.sh](http://run.sh)` to edit the script by adding in the Master’s address from before in the following spot:
    
    ```bash
    spark-submit  \
    --master <put master IP address here from earlier> \
    --executor-memory 512m \
    --driver-memory 512m \
    --files ~/cs643-pa2/TrainingDataset.csv \
    --class WineTrainer \
    target/wine-quality-spark-pa2-1.0-SNAPSHOT.jar || exit 1
    ```
    
- Now, in the master node again, run `chmod +x [run.sh](http://run.sh)` in cs643-pa2 directory
- Finally, you can start the job by executing `./run.sh`
- After the Spark job completes successfully, you will see that the machine learning model will be stored under the `wine_model` folder.

## Running the Docker Container

- Move the validation dataset you’d like to run into the cs643-pa2 folder
- run `chmod +x run_docker_predictor.sh`
- Run the script, while including the name of the dataset as an argument, i.e.
    
    `./run_docker_predictor.sh \ ValidationDataset.csv`
    
- Note: Ensure you have Docker installed on your system, or this script won’t be able to run successfully.