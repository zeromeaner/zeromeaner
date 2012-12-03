#!/bin/bash
mvn -P shade package
scp target/zeromeaner-all-*.jar www.zeromeaner.org:/var/www/zeromeaner/${DEV}/builds
mvn -P applet dependency:copy-dependencies package
scp target/zeromeaner.jar www.zeromeaner.org:/var/www/zeromeaner/${DEV}/zeromeaner.jar
scp target/dependency/*.jar www.zeromeaner.org:/var/www/zeromeaner/${DEV}/
scp -r src/main/site/* src/main/site/.htaccess www.zeromeaner.org:/var/www/zeromeaner/
