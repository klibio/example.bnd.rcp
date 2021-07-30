#!/bin/bash
set -x

# env validation
printf "\n# validate mandatory environment configuration variables\n\n"
if [[ -z "$DOCKER_USERNAME" || -z "$DOCKER_TOKEN" ]]; 
  then echo "missing ENV var DOCKER_USER and DOCKER_TOKEN"; exit 1; 
  else 
    echo "found env vars for DOCKER_USER=$DOCKER_USERNAME and DOCKER_TOKEN=<hidden>"; 
fi

# activate bash checks for unset vars, pipe fails
set -eauo pipefail

DATE=$(date +'%Y.%m.%d-%H.%M.%S')
IMAGE="klibio/example.bnd.rcp"
docker build \
  --no-cache \
  --progress=plain \
  --build-arg BUILD_DATE=$DATE \
  --build-arg VCS_REF=$(git rev-list -1 HEAD) \
  . -t "$IMAGE:$DATE" -t "$IMAGE:latest"

TEST_RESULT=$(pwd)/ressources
docker run -d \
  -e POP='1' \
  --mount type=bind,source=$TEST_RESULT,target=/data/target \
  --name test "$IMAGE:latest"

timeout=60
while [ ! -f $TEST_RESULT ];
do
  if [ "$timeout" == 0 ]; then
    echo "ERROR: timeout waiting for test result file $TEST_RESULT"
    exit 1
  fi
  sleep 1
  # Decrease the timeout of one
  ((timeout--))
done

input="$(pwd)/ressources/result.txt"
line=$(head -n 1 $input)


if [ "$line" = "true" ]
then
    echo "Application tests successful"
    docker stop test
    docker rm -f test

    echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin

    docker push "$IMAGE$DATE"
    docker push "${IMAGE}latest"

    exit 0
fi

docker stop test
docker rm -f test

echo "Application tests failed"
exit 1
