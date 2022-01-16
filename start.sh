#!/bin/bash

# ./start.sh  --data_path /tmp/data --dst_ip 127.0.0.1 --dst_port 3306 --dst_user root --dst_password 123456789

cd target

echo "shell parameters: $*"

java -Dfile.encoding=UTF-8 -classpath .:../lib/mysql-connector-java-8.0.27.jar:../lib/HikariCP-4.0.3.jar:../lib/slf4j-api-1.7.32.jar:../lib/jsqlparser-4.3-SNAPSHOT.jar:. com.sin.run  $*
