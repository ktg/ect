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

Structure:

src/
    equip/
        ect/ - common classes
            apps/ - applications
                ExporterGUI - java component hosting application
		    editor/ - editr util classes
		    editor/grapheditor/ - the  graphical editor
		    configurationmgr/ - the configuration manager (load/save arrangements of components)
                browser/Browser - the older "expert" browser/management application
                actmonitor - a for-example abstract activity monitor
            components/ - sample Java bean components
                associationlearner/ - learns a map or function by example
                blogger/ - blogger client, from ACCORD
                camera/ - triggered still camera (twain?), from ACCORD/DYNAMO?
                chat/ - simple text bean
                dynamo/
                    mediaviewer - media viewer, from DYNAMO, e.g ppt, mpg, avi
                fileexporter/ - export local file as url, from DYNAMO
                floatfunction/ - float fn of three floats; kind of scripting example
                floatvalue/ - input a float, e.g. for training
                slider/ - input an int, e.g. for training
                smartit/ - simple fixed smartit bean, from ACCORD
                text/ - simple text component
  		    [lots more]
            discovery/ - multicast server discovery support
            util/ - support classes (from DYNAMO?)
docs/ - docs
scripts/ - CVS template scripts
resources/ - supporting resources, e.g. external jars, dlls


Usage:

Build using ant

See docs/

See also Equip4cs [currently out of date]


Chris Greenhalgh
2004-03-01, updated 2005-05-02
