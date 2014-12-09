Running airline demo
=================

Please, firstly, call on your host, the script getdemotars.sh. This script will download from dropbox, all data files, and place them under /tmp/ansible_share



Then You need to update the script which launch you docker container for hadoopmaster, so that its include 2 additional directories:

* the directory /tmp/ansible_share : so that data files are visible from container
* the directory xxx/sparkrepo/airline-demo which contains the code


Thirdly, lauch your containers, and login in hadoopmaster.

Fourthly; bring data to hdfs:
on each file downloaded in first step, bring them to hdfs under /user/root

Then from airline-demo, run "mvn install", this will create a fat jar under target directory

finally:
spark-submit --class com.airline.PreProcessFlights --master yarn target/sparkwordcount-0.0.1-SNAPSHOT.jar output output

