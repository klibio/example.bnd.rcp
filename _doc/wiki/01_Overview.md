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
