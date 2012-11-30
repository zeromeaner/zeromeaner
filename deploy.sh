#!/bin/bash
mvn clean install
mvn -P applet dependency:copy-dependencies package
mvn -P shade package
scp target/zeromeaner.jar www.zeromeaner.org:/var/www/zeromeaner/{$DEV}/zeromeaner.jar
scp target/zeromeaner-all-*.jar www.zeromeaner.org:/var/www/zeromeaner/{$DEV}/builds
scp target/dependency/*.jar www.zeromeaner.org:/var/www/zeromeaner/{$DEV}/
scp -r src/main/site/* www.zeromeaner.org:/var/www/zeromeaner/
