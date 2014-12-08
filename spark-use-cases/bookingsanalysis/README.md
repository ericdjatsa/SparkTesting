Bookings Analysis
==============

A simple Spark application for bookings Analysis.

To make a jar:

    mvn package


Copy input data
Prepare hdfs paths and data : 

``` host>masterIPaddress=`sudo docker inspect -f "{{ .NetworkSettings.IPAddress }}" master`
    host> scp spark-use-cases/data/booking-data/demo-bookings.csv root@$masterIPaddress:~   (password is : "root")
    host> scp spark-use-cases/bookingsanalysis/target/bookinganalysis-0.0.1-SNAPSHOT.jar root@$masterIPaddress:~
    host> sudo ssh root@$masterIPaddress    (password is : "root")
    master> hdfs dfs -mkdir -p /user/root/data/input
    master> hdfs dfs -mkdir -p /user/root/data/output
    master> hdfs dfs -copyFromLocal ~/demo-bookings.csv /user/root/data/input
    
```
To run from a gateway node in a CDH5 cluster:

    spark-submit --class com.bigdatawave.bookingsanalysis.BookingsAnalysis --master local \
      bookingsanalysis-0.0.1-SNAPSHOT.jar <input file> <output path>

This will run the application in a single local process.  If the cluster is running a Spark standalone
cluster manager, you can replace "--master local" with "--master spark://`<master host>`:`<master port>`".

If the cluster is running YARN, you can replace "--master local" with "--master yarn".

Eg : 
- Run standard bookinganalysis 
    spark-submit --class com.bigdatawave.bookingsanalysis.BookingAnalysis --master yarn \
      bookingsanalysis-0.0.1-SNAPSHOT.jar hdfs:///user/root/data/input/demo-bookings.csv /user/root/data/output/standard
- Run bookinganalysis with Spark SQL
    spark-submit --class com.bigdatawave.bookingsanalysis.BookingAnalysisSQL --master yarn \
      bookingsanalysis-0.0.1-SNAPSHOT.jar hdfs:///user/root/data/input/demo-bookings.csv /user/root/data/output/sql


