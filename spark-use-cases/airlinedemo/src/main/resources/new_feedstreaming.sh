#!/bin/bash

# bring airline data to HDFS
hdfs dfs -copyFromLocal /tmp/ansible-share/demo/airline2008.csv input 

# Move the data to streaming directory in order to trigger streaming computation
hdfs dfs -mv input/airline2008.csv streaming
