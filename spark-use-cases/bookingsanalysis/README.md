Bookings Analysis
==============

A simple Spark application for bookings Analysis.

To make a jar:

    mvn package


Prepare config on host machine 

host> cd <PROJECT_REPO>/spark-setup
host> source init_env.sh
host> sudo su 
host> mybackupetchosts
host> mydockeretchosts >> /etc/hosts


Copy input data
Prepare hdfs paths and data : 

```   
    master> hdfs dfs -mkdir -p /user/root/input
    master> hdfs dfs -mkdir -p /user/root/output
    master> hdfs dfs -copyFromLocal /opt/data/demo-bookings.csv /user/root/input
    
```
To run from a gateway node in a CDH5 cluster:

```
    master> spark-submit --class com.bigdatawave.bookingsanalysis.BookingsAnalysis --master local \
      bookingsanalysis-0.0.1-SNAPSHOT.jar <input file> <output path>
```

This will run the application in a single local process.  If the cluster is running a Spark standalone
cluster manager, you can replace "--master local" with "--master spark://`<master host>`:`<master port>`".

If the cluster is running YARN, you can replace "--master local" with "--master yarn".

__Eg__ : 

```
* Run standard bookinganalysis 
    master> cd /opt/bookingsanalysis/src/main/resources
    master> ./runBookingAnalysis_standard.sh

* view results
     master> hdfs dfs -cat /user/root/output/standard/*


* Run bookinganalysis with Spark SQL
    master> cd /opt/bookingsanalysis/src/main/resources
    master> runBookingAnalysis_sql.sh

* view results
     master> hdfs dfs -cat /user/root/output/sql/*
```


Bookings Analysis Streaming ( to be run in Eclipse )


set Environment variable : 
SPARK_LOCAL_IP = <IP ADRESS OF HOST> OR 127.0.0.1

Run StreamingDataGenerator with following arguments : 
9999
/home/edy/Work/Projects/Spark/sparkrepoeric/spark-use-cases/data/booking-data/demo-bookings.csv
2         <--- stream 2 lines per second

Run BookingsAnalysisStreaming with following arguments : 

<IP ADRESS OF HOST> OR 127.0.0.1
9999
1        <--- 1 second time window
