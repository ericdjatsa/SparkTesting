docker stop slave1
docker rm slave1
docker run -d -t -h slave1.example.com --name slave1 --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) --dns-search example.com --link dns:dns --volumes-from dns pti1/sparkslave:secondversion
