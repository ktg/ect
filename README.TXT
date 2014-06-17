EQUATOR/EQUIP Component Toolkit (ECT), aka EQUIP Infrastructure

See http://sourceforge.net/projects/equip

This is a set of classes and applications for doing component-based system ("experience"?!) implementation, deployment and management. It uses EQUIP dataspace(s) for coordination and communication between native components (Java Beans, or IComponents in Equip4cs).

The model is one of software components that are "wired" together, and combines ideas from ACCORD, ECK and other component systems.

The initial designs/implementations responded to the EQUATOR IRC "Curious Home" project scenarios, but the infrastructure is more general, and subject to further future generalisation (e.g. for P2P).

The (original) component model design is described in docs/components-design.doc.

An initial implementation (esp. in terms of EQUIP shared data) is described in docs/components-types.doc.

The current codebase is a subset of this design...

To do (not essential :-):

* Generic (parameterised) Component Requests 

* Component request responses

* Property link requests based on component requests

* Property link responses

* Event linking between components

* Direct invocation linking between components in different containers

* Dataspace constants