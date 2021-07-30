#!/bin/bash
scriptDir="$( cd $( dirname $0 ) >/dev/null 2>&1 && pwd )"
stdout=$scriptDir/pop.stdout
result=$scriptDir/target/result.txt

if [ -z "$POP" ]
then 
    exit 0;
else
    printf "# executing PoP inside $scriptDir\n"
fi

function executeProcess {
  printf "## execute process $1\n"
  $1 & PID1=$!

  if [ -z "$PID1" ]
  then
      echo "false" > $stdout
      exit 1
  else
      echo "true" > $stdout
      printf "## successfully launched $1\n"
      kill $PID1
  fi
}

executeProcess "/data/jre/bin/java -jar /data/app.ui_linux.gtk.x86-64.jar"
executeProcess "/data/jre/bin/java -jar /data/ui_linux.gtk.x86-64.jar"
executeProcess "/data/jre/bin/java -jar /data/12_equinoxapp_linux.gtk.x86-64.jar"

echo "## copying execution result $stdout to mounted filesystem"
cp $stdout $result
echo "# done"
