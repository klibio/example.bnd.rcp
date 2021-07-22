# Project: example.bnd.eclipse
This GitHub repo shows the usage of Eclipse RCP framework with Bndtools.
Contained is a bndtools configuration referencing Eclipse Platform 4.18 and corresponding Simultaneous 2020.12 release.

Project `example.rcp.headless` contains a minimal console Eclipse RCP application.
Project `example.rcp.ui` contains a minimal Eclipse SWT UI application
Project `example.rcp.app.ui` contains a minimal Eclipse RCP UI application with a Platform specific product for win32, linux and macosx.


## License
Licensed under the [Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html).

## Short description of bndtools differences compared to Eclipse

### Target Management

Bndtools enables users to reference different repositories types [add link here].
We are using here 2 pre-indexed repositories (Platform and Simultaneous) of Eclipse release. 

#### Repositories


### Compilation
On each saving of a Java source file the corresponding class file is compiled and the bundle containing the package re-created. Result can be inspected inside the `generated` folder of the bnd project. If you open the jar file via double-click the `Jar File Viewer` allows you to introspect the contents of the jar file. Mind the 


### Run configuration
Mind `bndtools.runtim.eclipse.applaunch`



## Project content description

```
# /folder - <directory content description>
# file    - <purpose of the file>
 
/cnf                  -> bnd workspace containing the configuration for bnd	
/example.rcp.headless -> Headless RCP Application example
   /src               -> folder containing the java source files
   /.settings         -> folder containing the Eclipse project settings (per default hidden)
   /generated         -> folder containing the generated bundle jar file and bnd launch properties
   /launch            -> folder containing different Eclipse Launch configurations
      0_rcp.bndrun.launch                           - execute via bnd Launcher
      1_example.rcp.headless build.xml.launch       - ant export of a fat jar into folder _export
      2_run_exported_example.rcp.headless.launch    - execute exported fat jar as standalone java app
   /root              -> all files/folder contained inside will be stored inside the root of the bundle
      plugin.xml      - Eclipse plugin.xml file
   /_export           -> contains the exported folder
      example.rcp.headless.jar                      - exported jar file (only available after execution of 1_example...)
   /_rt              -> folder containing the Eclipse/OSGi runtime directories
      cfg            - Eclipse/OSGi configuration directory
      data           - Eclipse workspace directory
   .classpath                - classpath of the project (per default hidden)
   .gitignore                - Git ignore file (per default hidden)
   bnd.bnd                   - bnd project configuration (build path)
   build.xml                 - Ant file for exporting project fat jar file
   rcp.bndrun                - bnd run configuration
   rcpHeadless_JAR.bndrun    - bnd run configuration used for exporting fat jar (without arguments used inside the Eclipse IDE bndtools launch)
```

# Additions

## Update the repository index files

### 1. create a local fixed index of the used Eclipse repositories

General hint: Mind the line concatenation with `\` and comment all lines belonging together as well.

1. open the file `/cnf/build.bnd`
2. update the url's and versions inside the section `# Eclipse Target repositories`
3. un-comment the lines starting with `-plugin.1`
4. comment the lines starting with `-plugin.2`
5. save the file
6. verify that the bnd indexer has created 2 folders and containing index files inside the project folder `/cnf/fixedIndices`
7. you can use the local indexes - proceed to `### 2. use the locally created indexed files`

### 2. use the locally created indexed files

1. open the file `/cnf/build.bnd`
2. comment the lines starting with `-plugin.1`
3. un-comment the lines starting with `-plugin.2`
4. save the file
5. verify that inside the view `Repositories` the nodes for `Eclipse Platform <version>` and `Eclipse Simu Release <version>` can be expanded and have contents.


# Docker
```bash
#!/bin/bash
docker container run -d \
  -p 5800:5800/tcp \
  klibio/example.bnd.rcp
```
