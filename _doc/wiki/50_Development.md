This document describes the development technique used to develop this repo content.
It covers the topics from dev environment up do published container for production usage.

# develop in Eclipse with bndtools

# build Java / OSGi with gradle

Gradle in  with bndtools contributions make the build process straight forward. 
```bash
#!/bin/bash
# display all available gradle tasks
./gradlew tasks
```
The `Export tasks` section of the results lists multiple export tasks correlating to the bndrun files provided inside the projects.

The result of the export task in gradle is an executable jar file which is located in `<project>/generated/distributions/executable`

Supported Plstforms are: `linux.gtk.x86_64`,`win32.win32.x86_64` or `macosx.cocoa.x86_64`

## example.rcp.headless - EquinoxApp
To export the Eclipse Equinox Application (headless/cli) project execute
```bash
./gradlew export.headl
```

## example.rcp.ui - RCP UI
To export the RCP UI project execute
```bash
./gradlew export.ui_<yourPlatform>.x86-64
```

## example.rcp.app.ui - APP UI
To export the APP UI project execute
```
./gradlew export.app.ui_<yourPlatform>.x86-64
```

# build Docker container

# establish Continuous Build and Publish