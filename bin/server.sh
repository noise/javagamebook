#!/bin/sh
#
# shell script to run the GameServer
#
export CLASSPATH=./tmpclasses:lib/log4j-1.2.6.jar

# -server switch specifies that the Hotspot Server VM should be used
java -server -cp $CLASSPATH com.hypefiend.blackart2.server.GameServer
