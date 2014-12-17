### Work Env Setting ###

################
#   ALIASES    #
###############

### Expand aliases ###
# If a script is running in Bash environment , then we need to expand aliases
# before being able to use them in the script
shopt -s expand_aliases 2> /dev/null


#Backup /etc/hosts file
alias mybackupetchosts='sudo cp /etc/hosts /etc/bkp_hosts_`date +"%d%b%Y_%Hh%M"`'

# Clean /etc/hosts : remove docker containers names and IP adresses eventually inserted with the command above
alias mycleanetchosts='sudo sed -i -e "/.example.com/d" -e "/# Docker containers/d" /etc/hosts'

#### Docker commands #####
#Get a container's name and IP address
# usage : mydockerinfo <container name OR container ID >
# Example : > mydockerinfo master
alias mydockerinfo='sudo docker inspect --format "{{ .Config.Hostname }} {{ .NetworkSettings.IPAddress }} {{ .Id }}"'

# Get all containers' names and IP addresses
# usage : mydockerallinfo 
alias mydockerallinfo='sudo docker ps | tail -n +2 | while read cid restOfLine; do echo $cid; done | xargs sudo docker inspect --format "{{ .Config.Hostname }} {{ .NetworkSettings.IPAddress }}
"'

#Get containers' fully qualified names and IP addresses
# The output of this command is mainly intended to be piped to /etc/hosts file on the host machine
# in order to be able to access to services exposed in the docker containers from the host machine browse
alias mydockeretchosts=' { echo "# Docker containers" ; sudo docker ps | tail -n +2 | while read cid restOfLine; do echo $cid; done | xargs sudo docker inspect --format "{{ .NetworkSettings.IPAddress }}    {{ .Config.Hostname }}.{{ .Config.Domainname }}"; }'

# Connect through ssh to the master container
alias mydockergotomaster='ssh root@`sudo docker inspect -f "{{ .NetworkSettings.IPAddress }}" master`'


##################
#   FUNCTIONS    #
##################

#Input params
#$1 : container ID or container name
function isContainerRunning(){
	local _isRunning
	local cid_or_name
	_isRunning=`docker ps | grep "$cid_or_name"`
	echo "$_isRunning"
}