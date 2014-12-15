isRunning=`docker ps | grep "master"`

if [ $isRunning ];then

	docker stop master
	docker rm master
fi


docker run -d -t -h master.example.com --name master --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) --dns-search example.com --link dns:dns --volumes-from dns pti1/sparkmaster:secondversion
