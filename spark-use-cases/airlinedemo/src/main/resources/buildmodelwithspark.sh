#!/bin/bash

#1 - bring data file to hdfs
hadoop fs -mkdir input
hadoop fs -copyFromLocal /tmp/ansible-share/demo/airline2007.csv.bz2 input
hadoop fs -mkdir output

SPARK_ASSEMBLY_JAR="/usr/lib/spark/assembly/lib/spark-assembly-1.1.0-cdh5.2.0-hadoop2.5.0-cdh5.2.0.jar"

# run spark to build the model
/usr/lib/spark/bin/spark-submit --class com.airline.BuildAirlineDelayModel --master yarn --deploy-mode cluster --conf spark.yarn.jar=$SPARK_ASSEMBLY_JAR ../../../target/sparkairline-0.0.1-SNAPSHOT.jar input/airline2007.csv.bz2 output/airline.model output/airline.model_perf_metrics

# display performance metrics of the model
hdfs dfs -cat output/airline.model_perf_metrics/*