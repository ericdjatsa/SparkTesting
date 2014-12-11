Running airline demo - Flight Delays
=================
[sources]
http://hortonworks.com/blog/data-science-apacheh-hadoop-predicting-airline-delays/ 
http://hortonworks.com/blog/data-science-hadoop-spark-scala-part-2/
http://nbviewer.ipython.org/github/ofermend/IPython-notebooks/blob/master/blog-part-2.ipynb

Please, firstly, call on your host, the script getdemotars.sh. This script will download from dropbox, all data files, and place them under /tmp/ansible_share



Then You need to update the script which launch you docker container for hadoopmaster, so that its include 2 additional directories:

* the directory /tmp/ansible_share : so that data files are visible from container
* the directory xxx/sparkrepo/airline-demo which contains the code


Thirdly, lauch your containers, and login in hadoopmaster.

Fourthly; bring data to hdfs:
on each file downloaded in first step, bring them to hdfs under /user/root

Then from airline-demo, run "mvn install", this will create a fat jar under target directory

finally:
spark-submit --class com.airline.PreProcessFlights --master yarn target/sparkairline-0.0.1-SNAPSHOT.jar /user/root/output

printed output example : 
precision = 0.37, recall = 0.64, F1 = 0.47, accuracy = 0.59


External Libs : 
 
Simple Voronoi algorithm : 
http://ageeksnotes.blogspot.fr/2010/11/fast-java-implementation-fortunes.html
