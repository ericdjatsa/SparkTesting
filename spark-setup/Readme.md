#### HOW TO SETUP SPARK CLUSTER  

## CLUSTER INFO : 
- HADOOP version : Hadoop 2.5.0-cdh5.2.0
- Spark version  : 1.1.0
- Scala version  : 2.10.4 
- Java           : Java 1.7.0_65
- Java JDK       : OpenJDK 64-Bit Server VM
- Yarn resource manager Web UI : <master IP Adress>:8088
  

Je ne suis pas passé par cloudera manager.... j'ai essayé mais bof.

Dans le git repo j'ai cree un repertoire, ou se trouve les scripts pour lancer les containers dockers: SparkRepo / spark-setup / 
normalement tu les lances à la queue leu leu.... ils vont chopper les images dockers sur docker hub ( https://hub.docker.com/u/pti1/ )
puis tu fais : 

```
> sudo docker logs master
```

tu récupére son ip, puis tu ssh dessus ( user : root, passwd : root )

```
> ssh root@<IP Address>  ( user : root, passwd : root )

* For Spark Shell 

pour obtenir le spark-shell:
```
> MASTER=yarn-client 
> spark-shell
```

Ou directement
```
> spark-shell --master yarn-client
```

* pour lancer un job spark sur yarn 
```
> /usr/lib/spark/bin/spark-submit --class org.apache.spark.examples.SparkPi --master yarn-cluster --num-executors 3 /usr/lib/spark/examples/lib/spark-examples*.jar 3
```

c'est 2 commandes sont montrées dans les docker logs

* pour hive, il faut faire d'abord 
```
> rm -f /var/lib/hive/metastore/metastore_db/dbex.lck
```
