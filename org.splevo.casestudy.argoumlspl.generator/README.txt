
SPLevo Case Study: ArgoUML-SPL
==============================

Contact: Benjamin Klatt <klatt@fzi.de>

SPLevo Project: http://www.splevo.org

Overview
--------
ArgoUML-SPL a software product line version of the open source ArgoUML modeling tool.
http://argouml-spl.tigris.org/
Marcus Vinícius Couto, Marco Túlio Valente, and Eduardo Figueiredo have identified several optional 
features of ArgoUML and marked with JavaPP preprocessor markers in the source code.
http://www.slashdev.ca/javapp/

The SPLevo case study facilitates this adapted code to derive several variants with 
different sets of features present in the generated code.
Those variants are then investigated with the SPLevo tooling to identify and analyze
the variation points.

Further Information about the SPLevo ArgoUML Case Study can be found at: 
http://sdqweb.ipd.kit.edu/wiki/SPLevo/Case_Studies/ArgoUML-SPL

Variant Generator
-----------------
This eclipse project contains the facilities required to generate the variants.

build.xml 
This file is an ANT script integrating the JavaPP pre-processor tool and ANT task.
It can be used to derive different variants and produce new Eclipse Java projects with the 
source code of the variants.

License
-------
The JavaPP preprocessor is licensed under the GNU GPL.
We decided to integrate it's code into this project to simplify its use.

We would prefer to provide this generator under a really useful and open license
such as EPL or Apache. However, du to the integration, we are forced to provide this 
generator under the GNU GPL 