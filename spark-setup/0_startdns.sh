containerExists=`docker ps -a | grep "dns"`

if [ "$containerExists" ];then

	docker stop dns
	#Remove the container
	docker rm dns
fi



docker run -d -t --name dns -h dns.example.com -v /etc/bind pti1/bind9:secondversion

