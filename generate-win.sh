#!/bin/sh
# using git-bash on windows and the directory notation: /c/Projects/java2uml causes somewhere a nullpointer exception, so using the windows notation with \\,
# which by the way also works in cygwin terminal
java -jar target/java2uml.jar -packageInputConfigs="C:\\Projects\\java2uml\\packageInputConfigs-win.json" -outputDir="C:\\Projects\\java2uml"
