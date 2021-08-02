# Build

## execute local docker build

```bash
#!/bin/bash
DATE=$(date +'%Y.%m.%d-%H.%M.%S')
IMAGE="klibio/example.bnd.rcp"
docker build \
  --no-cache \
  --progress=plain \
  --build-arg BUILD_DATE=${DATE} \
  --build-arg VCS_REF=$(git rev-list -1 HEAD) \
  . -t "$IMAGE:$DATE" -t "${IMAGE}:latest"

# powershell
set-variable -name BUILD_DATE -value $(Get-Date -UFormat +'%Y-%m-%dT%H:%M:%SZ')
set-variable -name IMAGE -value "klibio/example.bnd.rcp"
docker build `
  --no-cache `
  --progress=plain `
  --build-arg BUILD_DATE=$BUILD_DATE `
  --build-arg VCS_REF=$(git rev-list -1 HEAD) `
  -t ${IMAGE}:latest . 
```

## Usage

To use a prepared container which includes all of the example projects and a GUI inside the browser execute the following snippet as script or command.

```bash

#!/bin/bash
docker container run -d \
  -p 5800:5800/tcp \
  klibio/example.bnd.rcp

# powershell
docker container run -d `
  -p 5800:5800/tcp `
  klibio/example.bnd.rcp
```

Please note, that due to Docker Hubs maximum duration of images there might not be a latest image available. Visit the [docker hub repository](https://hub.docker.com/repository/docker/klibio/example.bnd.rcp) or simply build the project yourself.
