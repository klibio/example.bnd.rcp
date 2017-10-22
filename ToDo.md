# Eclipse RCP with bndtools - Wednesday, October 25, 2017 - 10:30 to 11:05
 
This talk is for Eclipse RCP developers who want to learn the OSGi way of developing bundles.
If you are tired of setting targets and debugging PDE or target issues ;-) come and have a look at the alternative - bndtools.
Bndtools offers a great alternative with powerful concepts and opportunities.
We explain how to start developing a Eclipse 4.x application from scratch inside bndtools.
You see how you can deal with your dependencies in target setups on p2, file or maven based repositories.
Learn how to develop, debug and build your Eclipse RCP application based on the bnd workspace template for RCP developers.

## Goals

### Motivation - using modern bndtoolings for existiong Eclipse plugin universe

Why bndtools. What are the obstacles currently
Eclipse PDE Pain Points

- target reloading - uses only bundle names (not version specific)
- pde launches uses randomly bundles with same name and different versions
- IDE launch is not exported runtime product launch

What are the obstacles for using bndtools as Eclipse dev environment

- Missing p2 support
- Eclipse p2 features are not supported
	
#### KnowHow: Eclipse Feature Defintion
- container for OSGi bundles (XML file with platform specific information)
- Eclipse uses Singleton Directive - OSGi spec 10.1.15.110
	singleton installable unit / e.g. ExtensionPoint Provider, singleton IU org.eclipse.swt 

#### Storyline
- create bndtools workspace
- develop bnd bundles
- create bnd launches for execution and debugging
- build p2 repository with product constituted of features and the bnd builded bundles/plugins

#### Alternative bndtools
- export as fat jar
Open issues: How to update an bndtools build eclipse product?

#### Appendices: how to create a bndtools OSGi workspace cnf
- mirroring locally repos or reference p2 remote repos (Oomph - Repository Explorer)
- create bnd indices

### ToDo

- Mirror eclipse p2 repos ANT -> pde launch p2 mirror app
- [java] main -> [OSGi equinox] Headless Equinox App -> [OSGi bndtools] example DS immediate component
- Eclipse cnf project workspace template


