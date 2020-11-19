#!/bin/bash

case $1 in
"start"){
    for i in hadoop102 hadoop103 hadoop104
    do
        echo "------------- $i -------------"
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh start"
    done 
};;
"stop"){
    for i in hadoop102 hadoop103 hadoop104
    do
        echo "------------- $i -------------"
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh stop"
    done
};;
"status"){
    for i in hadoop102 hadoop103 hadoop104
    do
        echo "------------- $i -------------"
        ssh $i "/opt/module/zookeeper-3.5.7/bin/zkServer.sh status"
    done
};;
esac