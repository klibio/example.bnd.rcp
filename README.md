# example.bnd.eclipse

This repo shows the usage of Eclipse RCP framework with bndtools

## Project description

```

cnf                  - bnd workspace containing the configuration for bnd	
example.rcp.headless - Headless RCP Application example
   launch
      0_rcp.bndrun.launch                           -> execute via bnd launcher
      1_example.rcp.headless build.xml.launch       -> exports the fat jar into folder _export
      2_run_exported_example.rcp.headless.launch    -> run the exported fat jar as standalone java app
   _export - contains the exported folder
      example.rcp.headless.jar                      -> exported jar file

```

## License
Licensed under the [Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html).

