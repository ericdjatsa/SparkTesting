docker stop dns
docker rm dns
docker run -d -t --name dns -h dns.example.com -v /etc/bind pti1/bind9:secondversion

