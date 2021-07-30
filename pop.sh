#!/bin/bash
scriptDir="$( cd $( dirname $0 ) >/dev/null 2>&1 && pwd )"
result=$scriptdir/pop.stdout

if [ -z "$POP" ]
then 
    exit 0;
else
    printf "# executing PoP\n"
fi

function executeProcess {
  printf "# execute process $1\n"
  $1 & PID1=$!
  
  #check pid
  if [ -z "$PID1" ]
  then
      echo "false" > $result
      exit 1
  else
      kill $PID1
  fi
}

executeProcess "/data/jre/bin/java -jar /data/app.ui_linux.gtk.x86-64.jar"
executeProcess "/data/jre/bin/java -jar /data/ui_linux.gtk.x86-64.jar"
executeProcess "/data/jre/bin/java -jar /data/12_equinoxapp_linux.gtk.x86-64.jar"

echo "copy $result to mounted filesystem"
cp $result /data/target/result.txt
echo "done"
