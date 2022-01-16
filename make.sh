#!/bin/bash

mkdir -p target
javac -Xlint:unchecked -encoding utf-8 -classpath ./lib/mysql-connector-java-8.0.27.jar:./lib/HikariCP-4.0.3.jar:./lib/slf4j-api-1.7.32.jar:./lib/jsqlparser-4.3-SNAPSHOT.jar -sourcepath ./src/ ./src/com/sin/run.java  -d ./target/
