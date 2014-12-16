#!/bin/bash

#1 - bring data file to hdfs
hadoop fs -mkdir input
hadoop fs -copyFromLocal /tmp/ansible-share/demo/airline2007.csv.bz2 input
hadoop fs -mkdir output

# run spark to build the model
/usr/lib/spark/bin/spark-submit --class com.airline.BuildAirlineDelayModel --master yarn --deploy-mode cluster ../../../target/sparkairline-0.0.1-SNAPSHOT.jar input/airline2007.csv.bz2 output/airline.model
