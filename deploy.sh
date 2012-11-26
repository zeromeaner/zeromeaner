#!/bin/bash
mvn -P applet dependency:copy-dependencies package
scp target/zeromeaner.jar www.zeromeaner.org:/var/www/zeromeaner/zeromeaner.jar
scp target/dependency/*.jar www.zeromeaner.org:/var/www/zeromeaner/
scp -r src/main/site/* www.zeromeaner.org:/var/www/zeromeaner/
