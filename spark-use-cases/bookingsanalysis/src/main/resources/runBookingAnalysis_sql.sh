#!/bin/bash

hdfs dfs -mkdir -p /user/root/input
hdfs dfs -mkdir -p /user/root/output
hdfs dfs -copyFromLocal /opt/data/demo-bookings.csv /user/root/input 
hdfs dfs -rm -r output/sql

SPARK_ASSEMBLY_JAR="/usr/lib/spark/assembly/lib/spark-assembly-1.1.0-cdh5.2.0-hadoop2.5.0-cdh5.2.0.jar"

#Run application
spark-submit --class com.bigdatawave.bookingsanalysis.BookingAnalysisSQL --master yarn --conf spark.yarn.jar=$SPARK_ASSEMBLY_JAR \
      /opt/bookingsanalysis/target/bookinganalysis-0.0.1-SNAPSHOT.jar hdfs:///user/root/input/demo-bookings.csv /user/root/output/sql

# View results

hdfs dfs -cat output/sql/*


