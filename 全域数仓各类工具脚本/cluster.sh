#! /bin/bash

case $1 in
"start"){
	echo " -------- ���� ��Ⱥ -------"

	echo " -------- ���� hadoop��Ⱥ -------"
	/opt/module/hadoop-3.1.3/sbin/start-dfs.sh 
	ssh hadoop103 "/opt/module/hadoop-3.1.3/sbin/start-yarn.sh"

	#���� Zookeeper��Ⱥ
	zk.sh start

sleep 4s;

	#���� Flume�ɼ���Ⱥ
	f1.sh start

	#���� Kafka�ɼ���Ⱥ
	kf.sh start

sleep 6s;

	#���� Flume���Ѽ�Ⱥ
	f2.sh start

	};;
"stop"){
    echo " -------- ֹͣ ��Ⱥ -------"


    #ֹͣ Flume���Ѽ�Ⱥ
	f2.sh stop

	#ֹͣ Kafka�ɼ���Ⱥ
	kf.sh stop

    sleep 6s;

	#ֹͣ Flume�ɼ���Ⱥ
	f1.sh stop

	#ֹͣ Zookeeper��Ⱥ
	zk.sh stop

	echo " -------- ֹͣ hadoop��Ⱥ -------"
	ssh hadoop103 "/opt/module/hadoop-3.1.3/sbin/stop-yarn.sh"
	/opt/module/hadoop-3.1.3/sbin/stop-dfs.sh 
};;
esac