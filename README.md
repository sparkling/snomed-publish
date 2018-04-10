IHTSDO Snomed Publication Tools
===============================
Components
----------
* Canonical: Tool for creating the canonical form of the publication

* Test: Sanity test for the canonical form of the publication
    
* Web: Object graph browser for the canonical and long form of the publication. 

* Model:
        Library - Object model for snomed publish applications

*Import Export:
        Library - Marshall from long/short form serialisation formats, unmarshall to short form


How to build
------------
You will need to have the Java 7 JDK and Maven 3 to build the distribution jar file, and Java 7 JRE in order to run it. 
To build the distribution, enter the root project directory and type:

    mvn clean package
    
This will build all the individual libraries and applications. For each component, you will find the build results as
    
    {component}/target/{component}.jar

See the README for the individual libraries and applications for more information.
