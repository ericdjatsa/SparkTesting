#!/usr/bin/env bash
 
# Stop all components of the Hadoo-Spark cluster
# NB : run this script with sudo privileges !

#Input params
#$1 : stopped component name
function stoppingLog(){
	echo "Stopping : [ $@ ]"
} 

if [ "$(id -u)" != "0" ]; then
	echo "Insufficient privileges! Please run this script as root or with sudo."
	exit 1
fi

echo "Stopping Hadoop-Spark cluster ... "
 
echo -e "dns\n master\n slave1\n slave2" | while read cname ; do stoppingLog "$cname" ; docker stop $cname ; done

