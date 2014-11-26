docker stop slave2
docker rm slave2
docker run -d -t -h slave2.example.com --name slave2 --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) --dns-search example.com --link dns:dns --volumes-from dns pti1/sparkslave:secondversion
