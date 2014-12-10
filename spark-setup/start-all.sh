#!/usr/bin/env bash

script_basename=`basename $0`
AVAILABLE_DEMO_CONTEXTS="- bookings-analysis \n - flight-delays \n"
 
# Start all components of the Hadoo-Spark cluster
# NB : run this script with sudo privileges !

#Input params
#$1 : launched component name
function startingLog(){
	echo "Starting : [ $@ ]"
}

function usage() {
	echo "Usage : "
	echo "$script_basename <demo-context>"
	echo -e "\nAvailable demo contexts : \n"
	echo -e $AVAILABLE_DEMO_CONTEXTS
}

if [ "$(id -u)" != "0" ]; then
	echo "Insufficient privileges! Please run this script as root or with sudo."
	exit 1
fi

if [ $# -lt 1 ];then
	echo "Error! Missing parameters"
	usage
	exit 1
fi

# get demo context
demoContext=$1

# Get this script directory ( which is the spark-setup directory
SCRIPT_PATH=`readlink -f $0`
SPARK_SETUP_DIR=`dirname $SCRIPT_PATH`

# Start DNS server
startingLog "DNS SERVER"
$SPARK_SETUP_DIR/0_startdns.sh

# Start Master node and First slave in parallel
startingLog "SPARK MASTER"

# Choose the right MASTER according to the demo context
case $demoContext in 
	"bookings-analysis") $SPARK_SETUP_DIR/1_startmaster.sh &
	;;
	"flight-delays") $SPARK_SETUP_DIR/1_startmaster_flight_delays.sh &
	;;
	*) echo -e "Invalid demo-context. Valid demo-contexts are : $AVAILABLE_DEMO_CONTEXTS"
	exit 1
	;;
esac
	

startingLog "SPARK SLAVE 1"
$SPARK_SETUP_DIR/2_startfirstslave.sh 

# Start the second slave
startingLog "SPARK SLAVE 2"
$SPARK_SETUP_DIR/3_startsecondslave.sh

echo "Sourcing init_env.sh"
source $SPARK_SETUP_DIR/init_env.sh

echo "DONE!"