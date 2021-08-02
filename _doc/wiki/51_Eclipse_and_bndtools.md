## Short description of bndtools differences compared to Eclipse

### Target Management

Bndtools enables users to reference different repositories types [add link here].
We are using here 2 pre-indexed repositories (Platform and Simultaneous) of Eclipse release. 

#### Repositories
(see here https://bndtools.org/repositories.html)

### Compilation
On each saving of a Java source file the corresponding class file is compiled and the bundle containing the package re-created. Result can be inspected inside the `generated` folder of the bnd project. If you open the jar file via double-click the `Jar File Viewer` allows you to introspect the contents of the jar file. Mind the 


### Run configuration
Mind `bndtools.runtim.eclipse.applaunch`
