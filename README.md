What
=====
CLI tool to generate UML from Java Code. There is currently a small bug, which makes it fail on linux.

How
=====

build
======
mvn clean install
During the build a sample graphviz dot-file will be generated in target/io.neocdtv.modelling.reverse.domain representing the internal custom model. The easiest way to view it is to go to http://viz-js.com/ and paste its content.

run
======
The build will generate a jar, which can be used like:
usage: java -jar target/java2uml.jar -packages=... -sourceDir=... -outputFile=... [-r] [-ecore]
options:
        -r      recursive package scanning
        -ecore  use Ecore internally (alpha)
        
example: java -jar target/java2uml.jar -packages=io.neocdtv.modelling.reverse.domain -sourceDir=src/main/java -outputFile=output.dot -r
This will generate a representation of the simple test domain-model. To view it go to http://viz-js.com/ and paste its content.

