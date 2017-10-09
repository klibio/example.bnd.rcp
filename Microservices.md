# Microservices with OSGi - Tuesday, October 24, 2017 - 14:30 to 15:05

OSGi declarative services exist for a long time and are used to implement a modular service-oriented architecture. Because of the supported dynamics, the easy way to define, register and consume services, declarative services can be found in various scenarios. Using several of the long time existing specifications like Declarative Service, ConfigurationAdmin, EventAdmin and Remote Service Admin, it is also easy to setup the currently hyped micro services by using OSGi declarative services.

In this session we will give an introduction to OSGi Remote Services and the Remote Service Admin. We will explain the basic components in these specs and how they play together. And we will set up a small example to show in code how easy it is making OSGi services available as microservices using an OSGi framework, Remote Service Admin implementations like ECF and toolings like Bndtools or PDE.
We will show examples in a way that both Eclipse RCP and plain OSGi developers can benefit. So the required steps will be shown in PDE and Bndtools in parallel. The main focus is to understand how microservices can be created using OSGi standards. The tooling shouldn't be the limiting factor.

## Goals

### Motivation

- everybody does Microservices ;-) wheter it fits or not
- OSGi has matured Service systems - OSGI cmpn release 6
	100 Remote Services page 21
	122 RemoteServiceAdminServiceSpecification page 743
- Many Architecure talks - no in-depth technical description

### Storyline

- Terminology
Services, Remote Services, Discovery, Distribution/Topology and Endpoint

- How to implement
- Configuration OSGi runtime

available implementation: Apache CXF, Eclise ECF, Amdatu, Paremus RSA/Gossip

Showcase/Demo

When to use it? Complexity

#### Appendices
Neil Bartlett - https://www.slideshare.net/mfrancis/scaling-and-orchestrating-microservices-with-osgi-n-bartlett
Christian Schneider - https://www.slideshare.net/mfrancis/lean-microservices-with-osgi-christian-schneider


 