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

