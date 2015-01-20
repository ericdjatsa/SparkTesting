# Error status for incorrect usage of a script - eg. : not called with correct parameters
E_USAGE="1"
usageMsg="Usage : $0 -n <container name> [-v <volume1> -v <volume2> ... ]"
bindVolumes=""
# Get input parameters
while [ $# -gt 0 ] ; do
		case $1 in
		"-n"|"--name")
			if [ $# -eq 1 ]; then
				echo "Missing attribute for $1 option!!!" 
				echo $usageMsg 
				return $E_USAGE
			fi
			container_name=$2
			shift;;
			
		"-v"|"--volume")
			if [ $# -eq 1 ]; then
				echo "Missing attribute for $1 option!!!"
				echo $usageMsg 
				return $E_USAGE
			fi
			
			#Prepare bind volumes
			# add '-v' in front of each volume name
			# Ex : " vol1 vol2 vol3 vol4 " --> " -v vol1 -v vol2 -v vol3 -v vol4"
			bindVolumes="$bindVolumes -v $2"
			shift;;
		*)  echo "Unknown parameter $1"
			echo $usageMsg 
			return $E_USAGE;;
		esac
		shift
done

# Check input arguments

if [ -z "$container_name" ];then
	echo "Error! Missing container name. Script will exit ..."
	exit 1
fi

containerExists=`docker ps -a | grep "$container_name"`

if [ "$containerExists" ];then
	docker stop $container_name
	# Remove the container
	docker rm $container_name
fi


sudo docker run -d -t -h $container_name.example.com  $bindVolumes --name $container_name --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) --dns-search example.com --link dns:dns --volumes-from dns pti1/sparkmaster:secondversion
