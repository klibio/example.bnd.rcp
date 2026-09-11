#!/bin/bash
set -euo pipefail

if [[ "$#" -ne 1 ]]; then
    printf 'usage: %s <application-jar>\n' "$0" >&2
    exit 2
fi

export DISPLAY="${DISPLAY:-:0}"
exec /data/jre/bin/java -jar "$1"