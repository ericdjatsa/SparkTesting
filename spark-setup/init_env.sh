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


# Print column names for docker info command
function mydockerinfo_header () {
  # Print column names
  echo "Hostname , Domainname, IPAddress , Container ID, Linked containers" 
}

#### Docker commands #####

# Get a container's name, IP address , ID, and linked containers
# usage : mydockerinfo_base <container name OR container ID >
# Example : > mydockerinfo_base master
# Parameters : 
# #$1 : container name
function mydockerinfo_base() {
  local _container_name=$1
  # Print container info
  sudo docker inspect --format "{{ .Config.Hostname }} , {{ .Config.Domainname }} , {{ .NetworkSettings.IPAddress }} , {{ .Id }} , {{ .HostConfig.Links }}" $_container_name
}

# Print a container's name, IP address , ID, and linked containers in csv format
# with an information header containing the column names printed above
# usage : mydockerinfo_csv <container name OR container ID >
# Example : > mydockerinfo_csv master
# Parameters : 
# #$1 : container name
function mydockerinfo_csv() {
  local _container_name=$1
  # Print column names
  mydockerinfo_header
  # Print container info
  mydockerinfo_base $_container_name
}

# Pretty print helper for the above function
# Display the info in a tabular format
# Parameters : 
# #$1 : container name
function mydockerinfo() {
  local _container_name=$1
  mydockerinfo_csv $_container_name | column -s ',' -t
}

# Get all containers' name, IP address , ID, and linked containers in csv format
# usage : mydockerallinfo 
function mydockerallinfo_csv() {

  # Print column names
  mydockerinfo_header; 
  sudo docker ps | tail -n +2 | while read cid restOfLine; do mydockerinfo_base $cid; done
}

# Pretty print helper for the above function
# Display the info in a tabular format
function mydockerallinfo() {
  mydockerallinfo_csv | column -s ',' -t
}


#Get containers' fully qualified names and IP addresses
# The output of this command is mainly intended to be redirected ( with >> ) to /etc/hosts file on the host machine
# in order to be able to access to services exposed in the docker containers from the host machine browse
# Example : mydockeretchosts >> /etc/hosts
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