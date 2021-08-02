## Introduction
This repository uses Github Actions to start a script which executes all necessary build and deploy steps within the Actions virtual machine. This is done to escape version dependencies of Github Action Docker Tasks and reduce maintenance. A short explanation to the start script is given below.

## Important script aspects

The start script requires two environmental variables to be set: DOCKER_USER and DOCKER_TOKEN, to be able to push the artifacts to the Docker Repository.

After the docker build the container is started with this command:
```
docker run -d -e POP='1' --mount type=bind,source="$(pwd)"/pop,target=/data/target --name test "klibio/example.bnd.rcp:latest"
```

This starts the container in test mode, which will execute a test script and give the results back inside a test file in the pop folder inside the repository. This is solely used to give a Proof of Performance in the pipeline process. If the PoP fails, so does the pipeline and the artifact is not pushed.

The latest image is tagged latest and with the build time, formatted 'YYYY.mm.dd-HH.MM.SS'