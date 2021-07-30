#!/bin/bash

set -x

if [ -z "$POP" ]
then 
    exit 0;
fi

#execute process 1
/data/jre/bin/java -jar /data/app.ui_linux.gtk.x86-64.jar & PID1=$!

#check pid 1
if [ -z "$PID1" ]
then
    echo "false"
    exit 1
else
    kill $PID1
fi
#execute process 2
/data/jre/bin/java -jar /data/ui_linux.gtk.x86-64.jar & PID2=$!

#check pid 2
if [ -z "$PID2" ]
then
    echo "false"
    exit 1
else
    kill $PID2
fi

#execute process 3
/usr/bin/x-terminal-emulator -e /data/jre/bin/java -jar /data/12_equinoxapp_linux.gtk.x86-64.jar & PID3=$!

#check pid 3
if [ -z "$PID3" ]
then
    echo "false"
    exit 1
else
    echo "true"
    kill $PID3
fi
#copy result to mounted filesystem
cp /data/pop.stdout /data/target/result.txt

#output of this file is in /data/pop.stdout
