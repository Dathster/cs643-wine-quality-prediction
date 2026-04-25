FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y wget curl

WORKDIR /cs643-pa2

COPY target/wine-quality-spark-pa2-1.0-SNAPSHOT.jar app.jar
COPY wine_model/ wine_model/

RUN wget https://downloads.apache.org/spark/spark-3.5.8/spark-3.5.8-bin-hadoop3.tgz \
	&& tar -xzf spark-3.5.8-bin-hadoop3.tgz \
	&& mv spark-3.5.8-bin-hadoop3 spark \
	&& rm spark-3.5.8-bin-hadoop3.tgz

ENV SPARK_HOME=/cs643-pa2/spark
ENV PATH=$SPARK_HOME/bin:$PATH

ENTRYPOINT ["spark-submit", "--class", "WineRunner", "app.jar"]  
