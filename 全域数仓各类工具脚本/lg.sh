#! /bin/bash

for i in hadoop102 hadoop103 
do
	ssh $i "java -jar /opt/module/log-collector-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 >/dev/null 2>&1 &"
done