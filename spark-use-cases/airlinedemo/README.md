Running airline demo
=================

On your localhost
   
*firstly, call on your host, the script ./getdemotars.sh. This script will download from dropbox, all data files, and place them under /tmp/ansible_share
* in eclise run maven install to generate the fat jar file
*update your docker scripts for hadoopmaster, so that its include 2 additional directories:

** the directory /tmp/ansible_share : so that data files are visible from container
** the directory xxx/sparkrepo/airline-demo which contains the code

In Hadoop Master container

ssh root@<master IP adress>

* first call script src/main/resources/buildmodelwithsparks.sh : it will generate the model

* secondnly call script src/main/resources/runModelInStreaming.sh

* then unzip data from 2008

* finally call src/main/resources/feedstreaming.sh


TODO : Add in /etc/hosts of host machine the IP adress of master and slaves

master.example.com 172.17.0.2

slave1.example.com 172.17.0.3

The accumulators are shown in the last stage in Spark UI. 



