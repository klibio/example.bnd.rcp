#!/bin/bash

# env validation
printf "\n# validate mandatory environment configuration variables\n\n"
if [[ -z "$DOCKER_USERNAME" || -z "$DOCKER_TOKEN" ]]; 
  then echo "  missing ENV var DOCKER_USER and DOCKER_TOKEN"; exit 1; 
  else echo "  found mandatory env vars for DOCKER_USER=$DOCKER_USERNAME and DOCKER_TOKEN=<hidden>"; 
fi

# activate bash checks for unset vars, pipe fails
set -eauo pipefail

DATE=$(date +'%Y.%m.%d-%H.%M.%S')
IMAGE="klibio/example.bnd.rcp"
echo "# launching docker build for image $IMAGE at $DATE"
docker build \
  --no-cache \
  --progress=plain \
  --build-arg BUILD_DATE=$DATE \
  --build-arg VCS_REF=$(git rev-list -1 HEAD) \
  -t "$IMAGE:$DATE" \
  -t "$IMAGE:latest" \
  .

POP_CONTAINER=popContainer
echo "# launching container for PoP - $POP_CONTAINER"
POP_RESULT_DIR=$(pwd)/ressources
POP_RESULT=$POP_RESULT_DIR/result.txt
chmod go+w $POP_RESULT_DIR
rm -rf $POP_RESULT
ls -l $PWD
docker run -d \
  -e POP='1' \
  -p 5800:5800/tcp \
  --mount type=bind,source=$POP_RESULT_DIR,target=/data/target \
  --name $POP_CONTAINER \
  "$IMAGE:$DATE"

echo "# display container logs for $IMAGE"
CONTAINER_ID=$(docker ps -aqf "ancestor=$IMAGE")
docker logs -f $CONTAINER_ID &

timeout=60
while [ ! -f $POP_RESULT ];
do
  if [ "$timeout" == 0 ]; then
    echo "ERROR: timeout waiting for PoP result file $POP_RESULT"
    exit 1
  fi
  sleep 1
  # decrease the timeout of one
  ((timeout--))
done

input="$POP_RESULT"
line=$(head -n 1 $input)

if [ "$line" = "true" ]
then
    echo "# PoP successful - stopping container"
    docker stop $POP_CONTAINER
    docker rm -f $POP_CONTAINER

    echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin
    docker push "$IMAGE:$DATE"
    docker push "$IMAGE:latest"
    echo "# successfully pushed image to DockerHub https://hub.docker.com/r/$IMAGE"
    exit 0
fi

docker stop test
docker rm -f test

echo "PoP failed - aborting build"
exit 1
