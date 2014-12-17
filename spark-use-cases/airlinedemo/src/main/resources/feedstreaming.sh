#!/bin/bash

#unzip manually airline 2008 dataset from your host
# package bzip2 provide bunzip2

rm -rf /tmp/tempodata
mkdir /tmp/tempodata
oldfilenb=$((0))


cnt=0
while read LINE
do
   cnt=$((cnt+1))
   
   filenb=$((cnt/100))

   if [ $filenb -gt $oldfilenb ];
   then
     echo "Streaming 100 flights data in airline2008-$oldfilenb.csv to HDFS ... "
     hadoop fs -copyFromLocal /tmp/tempodata/airline2008-$oldfilenb.csv streaming/airline2008-$oldfilenb.csv
     sleep 5
   fi  

   echo $LINE >> /tmp/tempodata/airline2008-$filenb.csv

   oldfilenb=$((filenb))

done < /tmp/ansible-share/demo/airline2008.csv

