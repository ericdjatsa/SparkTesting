==== HOW TO SETUP SPARK CLUSTER ===
Je ne suis pas passé par cloudera manager.... j'ai essayé mais bof.

dans le git repo j'ai cree un repertoire, ou se trouve les scripts pour lancer les containers dockers: SparkRepo / spark-setup / 
normalement tu les lances à la queue leu leu.... ils vont chopper les images dockers sur docker hub (je suis toujours en train de les pousser)
puis tu fais : 
# docker logs master

tu récupére son ip, puis tu ssh dessus....
/* For Spark Shell */
pour obtenir le spark-shell:MASTER=yarn-client spark-shell

/* pour lancer un job spark sur yarn */ 
/usr/lib/spark/bin/spark-submit --class org.apache.spark.examples.SparkPi --master yarn-cluster --num-executors 3 /usr/lib/spark/examples/lib/spark-examples*.jar 3

c'est 2 commandes sont montrées dans les docker logs
/* pour hive, il faut faire d'abord */
> rm -f /var/lib/hive/metastore/metastore_db/dbex.lck
