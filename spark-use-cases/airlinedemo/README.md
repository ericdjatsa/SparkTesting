Running airline demo
=================

On your localhost

* Copy already downloaded airline data from ~/tmp/ansible-share/ to /tmp/ansible-share

> sudo cp -r ~/tmp/ansible-share/ /tmp
   
 OR 

*firstly, call on your host, the script ./getdemotars.sh. This script will download from dropbox, all data files, and place them under /tmp/ansible_share
* Once the download is finished, unzip data from 2008 
> sudo bunzip2 /tmp/ansible-share/demo/airline2008.csv.bz2

* in eclise run maven install to generate the fat jar file
*update your docker scripts for hadoopmaster, so that its include 2 additional directories:

** the directory /tmp/ansible_share : so that data files are visible from container
** the directory xxx/sparkrepo/airline-demo which contains the code

* backup the /etc/hosts file of you host machine
> mybackupetchosts

* clean /etc/hosts : remove old info from docker containers. Eg. fully qualified domain name and IP adresses
> mycleanetchosts

* Add into your host machine's /etc/hosts file, the bindings to all the docker containers fully qualified domain name and IP adresses
> sudo su
> mydockeretchosts >> /etc/hosts

In Hadoop Master container

ssh root@<master IP adress>

* first call script src/main/resources/buildmodelwithsparks.sh : it will generate the model
> cd /opt/airlinedemo/src/main/resources/
> ./buildmodelwithspark.sh

* secondnly call script src/main/resources/runModelInStreaming.sh
> runModelInStreaming.sh

* finally call src/main/resources/feedstreaming.sh
> feedstreaming.sh

* Monitor the execution on Yarn, view the total count of good and bad predictions on the last stage panel in the ACCUMULATORS section  
http://master.example.com:8088

* Then display the results on HDFS
http://master.example.com:50070/explorer.html#/user/root/results







