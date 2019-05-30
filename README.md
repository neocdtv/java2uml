What
=====
CLI tool to generate a Eclipse Uml or Eclipse Ecore (also as Json) from Java source code


How
=====

checkout and build dependent project(s) (not present in maven reps)
======
        git clone https://github.com/neocdtv/uml-io.git
        cd ./uml-io
        mvn clean install
build
======
        cd ./java2uml  
        mvn clean install
run
======
        java -jar target/java2uml.jar -packageInputConfigs="packageInputConfigs.json" -outputDir=. -outputFormat=UML

PackageInputConfigs is a small json config, where you can define, which packages at which location should be processed. You can find an example in the project.
