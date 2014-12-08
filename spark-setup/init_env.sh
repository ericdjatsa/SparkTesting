### Work Env Setting ###

################
#   ALIASES    #
###############

### Expand aliases ###
# If a script is running in Bash environment , then we need to expand aliases
# before being able to use them in the script
shopt -s expand_aliases 2> /dev/null

#### Docker commands #####
#Get a container's name and IP address
# usage : mydockerinfo <container name OR container ID >
# Example : > mydockerinfo master
alias mydockerinfo='sudo docker inspect --format "{{ .Config.Hostname }} {{ .NetworkSettings.IPAddress }} {{ .Id }}"'

# Get all containers' names and IP addresses
# usage : mydockerallinfo 
alias mydockerallinfo='sudo docker ps | tail -n +2 | while read cid restOfLine; do echo $cid; done | xargs sudo docker inspect --format "{{ .Config.Hostname }} {{ .NetworkSettings.IPAddress }}
"'
 
