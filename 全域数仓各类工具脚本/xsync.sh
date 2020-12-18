#!/bin/bash
#1. �жϲ�������
if [ $# -lt 1 ]
then
  echo Not Enough Arguement!
  exit;
fi
#2. ������Ⱥ���л���
for host in hadoop102 hadoop103 hadoop104
do
  echo ====================  $host  ====================
  #3. ��������Ŀ¼����������
  for file in $@
  do
    #4 �ж��ļ��Ƿ����
    if [ -e $file ]
    then
      #5. ��ȡ��Ŀ¼
      pdir=$(cd -P $(dirname $file); pwd)
      #6. ��ȡ��ǰ�ļ�������
      fname=$(basename $file)
      ssh $host "mkdir -p $pdir"
      rsync -av $pdir/$fname $host:$pdir
    else
      echo $file does not exists!
    fi
  done
done