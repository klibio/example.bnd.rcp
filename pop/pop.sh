#!/bin/bash
set -u

scriptDir="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
stdout=$scriptDir/pop.stdout
result=$scriptDir/target/result.txt

if [ -z "$POP" ]
then 
    exit 0;
else
    printf "# executing PoP inside $scriptDir\n"
fi

executeProcess() {
    local jarPath="$1"
    printf "## execute process %s\n" "$jarPath"
    /data/run-app.sh "$jarPath" &
    local pid=$!

    if kill -0 "$pid" 2>/dev/null
  then
            printf "true\n" > "$stdout"
            printf "## successfully launched %s\n" "$jarPath"
            kill "$pid" 2>/dev/null || true
            wait "$pid" 2>/dev/null || true
  else
            printf "false\n" > "$stdout"
            printf "## failed to launch %s\n" "$jarPath"
            exit 1
  fi
}

executeProcess /data/app.ui_linux.gtk.x86-64.jar
executeProcess /data/ui_linux.gtk.x86-64.jar
executeProcess /data/12_equinoxapp_linux.gtk.x86-64.jar

echo "## copying execution result $stdout to mounted filesystem"
cp "$stdout" "$result"
echo "# done"
