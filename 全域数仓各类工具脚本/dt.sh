#!/bin/bash

for i in hadoop102 hadoop103 hadoop104
do
    echo "========== $i =========="
    ssh -t $i "sudo date -s $1"
done