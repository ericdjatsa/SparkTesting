docker stop master
docker rm master
# Run master with additional host folders
docker run -d -t -v /tmp/ansible-share:/tmp/ansible-share  -v /home/edy/Work/Projects/Spark/sparkrepoeric/airline-demo:/opt/airline-demo -h master.example.com --name master --dns $(docker inspect -f '{{.NetworkSettings.IPAddress}}' dns) --dns-search example.com --link dns:dns --volumes-from dns pti1/sparkmaster:secondversion 
