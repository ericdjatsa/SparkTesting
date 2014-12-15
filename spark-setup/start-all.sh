#!/usr/bin/env bash

script_basename=`basename $0`
 
# Start all components of the Hadoo-Spark cluster
# NB : run this script with sudo privileges !

#Input params
#$1 : launched component name
function startingLog(){
	echo "Starting : [ $@ ]"
}

function usage() {
	echo "Usage : "
	echo "$script_basename <cloned Git repository root dir>"
}

if [ "$(id -u)" != "0" ]; then
	echo "Insufficient privileges! Please run this script as root or with sudo."
	exit 1
fi

EXPECTED_NUM_INPUT_PARAMS=1

if [ $# -lt $EXPECTED_NUM_INPUT_PARAMS ];then
	echo "Error! Missing input parameters"
	usage
	exit 1
fi


# get repository root dir parameter
REPO_ROOT_DIR=`readlink -f $1`

if [ ! -d $REPO_ROOT_DIR ];then
	echo "Error directory REPO_ROOT_DIR = [ $REPO_ROOT_DIR  ] does not exists or is not valid! Script will exit ..."
	exit 1
fi


# Get this script directory ( which is the spark-setup directory
SCRIPT_PATH=`readlink -f $0`
SPARK_SETUP_DIR=`dirname $SCRIPT_PATH`

echo "Sourcing init_env.sh"
source $SPARK_SETUP_DIR/init_env.sh


# Start DNS server
startingLog "DNS SERVER"
$SPARK_SETUP_DIR/0_startdns.sh

# Start Master node and First slave in parallel
startingLog "SPARK MASTER"

$SPARK_SETUP_DIR/startnode.sh -n "master" -v $REPO_ROOT_DIR/spark-use-cases/bookingsanalysis:/opt/bookingsanalysis -v $REPO_ROOT_DIR/airline-demo:/opt/airline-demo -v /tmp/ansible-share:/tmp/ansible-share &
	

startingLog "SPARK SLAVE 1"
$SPARK_SETUP_DIR/startnode.sh -n "slave1"

# Start the second slave
startingLog "SPARK SLAVE 2"
$SPARK_SETUP_DIR/startnode.sh -n "slave2"

echo "DONE!" 
