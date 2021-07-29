# Project description

[![docker_build](https://github.com/klibio/example.bnd.rcp/actions/workflows/actions_build.yml/badge.svg)](https://github.com/klibio/example.bnd.rcp/actions/workflows/actions_build.yml)
[![Docker Hub](https://img.shields.io/badge/Docker%20Hub-example.bnd.rcp-blue)](https://hub.docker.com/repository/docker/klibio/example.bnd.rcp)
[![Project](https://img.shields.io/badge/Project-Wiki-blueviolet)](https://github.com/klibio/example.bnd.rcp/wiki)

This project aims at providing multiple examples on the creation of RCP Applications with bndtools. The different usecases are headless, a time-based UI and an application UI. See the project wiki above for more information on using, building and more!

# Try it out with Docker
```bash
#!/bin/bash
docker container run -d \
  -p 5800:5800/tcp \
  klibio/example.bnd.rcp
```
