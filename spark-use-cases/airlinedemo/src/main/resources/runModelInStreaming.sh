#!/bin/bash

#1 - bring data file to hdfs
hadoop fs -rm -r -f streaming
hadoop fs -mkdir streaming
hadoop fs -chmod -R 777 streaming
hadoop fs -rm -r -f results


SPARK_ASSEMBLY_JAR="/usr/lib/spark/assembly/lib/spark-assembly-1.1.0-cdh5.2.0-hadoop2.5.0-cdh5.2.0.jar"

# run spark streaming
# monitor hdfs folder streaming
# try to predict delay
/usr/lib/spark/bin/spark-submit --class com.airline.PredictDelayStreaming --master yarn --deploy-mode cluster --conf spark.yarn.jar=$SPARK_ASSEMBLY_JAR --conf spark.ui.killEnabled=true  ../../../target/sparkairline-0.0.1-SNAPSHOT.jar streaming output/airline.model results


