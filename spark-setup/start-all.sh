#!/usr/bin/env bash
 
# Start all components of the Hadoo-Spark cluster
# NB : run this script with sudo privileges !

#Input params
#$1 : launched component name
function startingLog(){
	echo "Starting : [ $@ ]"
}

if [ "$(id -u)" != "0" ]; then
	echo "Insufficient privileges! Please run this script as root or with sudo."
	exit 1
fi

# Get this script directory ( which is the spark-setup directory
SCRIPT_PATH=`readlink -f $0`
SPARK_SETUP_DIR=`dirname $SCRIPT_PATH`

# Start DNS server
startingLog "DNS SERVER"
$SPARK_SETUP_DIR/0_startdns.sh

# Start Master node and First slave in parallel
startingLog "SPARK MASTER"
$SPARK_SETUP_DIR/1_startmaster.sh &

startingLog "SPARK SLAVE 1"
$SPARK_SETUP_DIR/2_startfirstslave.sh 

# Start the second slave
startingLog "SPARK SLAVE 2"
$SPARK_SETUP_DIR/3_startsecondslave.sh

echo "Sourcing init_env.sh"
source $SPARK_SETUP_DIR/init_env.sh

echo "DONE!"