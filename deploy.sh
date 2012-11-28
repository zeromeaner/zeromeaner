#!/bin/bash
mvn clean install
mvn -P applet dependency:copy-dependencies package
mvn -P shade package
scp target/zeromeaner.jar www.zeromeaner.org:/var/www/zeromeaner/zeromeaner.jar
scp target/zeromeaner-all-*.jar www.zeromeaner.org:/var/www/zeromeaner/builds
scp target/dependency/*.jar www.zeromeaner.org:/var/www/zeromeaner/
scp -r src/main/site/* www.zeromeaner.org:/var/www/zeromeaner/
