# Project description

[![docker_build](https://github.com/klibio/example.bnd.rcp/actions/workflows/actions_build.yml/badge.svg)](https://github.com/klibio/example.bnd.rcp/actions/workflows/actions_build.yml)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-example.bnd.rcp-blue)](https://hub.docker.com/repository/docker/klibio/example.bnd.rcp)
[![Project](https://img.shields.io/badge/Project-Wiki-blueviolet)](https://github.com/klibio/example.bnd.rcp/wiki)

This projects exemplifies the development of [Eclipse 4 RCP Application](https://www.eclipse.org/equinox/) with [bndtools](https://bndtools.org/).
It enables
* local development with Eclipse IDE and bndtools
* continuous building and ProofOfPerformance(pop) with GitHub actions
* running inside Debian based Docker container with web browser accessible UI

Multiple minimalistic Eclipse RCP project types are demonstrated
1. headless application - targeted for terminal usage
2. [SWT](https://www.eclipse.org/swt/) dialog UI 
3. Branded Eclipse 4 product SWT UI application with UI. 

See the project [![Project](https://img.shields.io/badge/Project-Wiki-blueviolet)](https://github.com/klibio/example.bnd.rcp/wiki) for more information on using, building and more!

# Try it out ( local [Docker](https://www.docker.com/) installation required)

## launch the application container with
```bash
#!/bin/bash
docker container run -d \
  -p 5800:5800/tcp \
  klibio/example.bnd.rcp
```
```powershell
# powershell
docker container run -d `
  -p 5800:5800/tcp `
  klibio/example.bnd.rcp
```

##  Access the UI via  web browser http://localhost:5800 
1. connect to VNC
2. use context-menu (right-mouse) to launch applications
