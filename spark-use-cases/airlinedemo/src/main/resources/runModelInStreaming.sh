#!/bin/bash

#1 - bring data file to hdfs
hadoop fs -rm -r -f streaming
hadoop fs -mkdir streaming
hadoop fs -chmod -R 777 streaming


# run spark streaming
# monitor hdfs folder streaming
# try to predict delay
/usr/lib/spark/bin/spark-submit --class com.airline.PredictDelayStreaming --master yarn --deploy-mode cluster ../../../target/sparkairline-0.0.1-SNAPSHOT.jar streaming output/airline.model results
