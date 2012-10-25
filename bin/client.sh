#!/bin/sh
#
# shell script to run the RPSClient
# usage: client.sh <server_ip> <username>
#
java -cp ./tmpclasses:lib/log4j-1.2.6.jar com.hypefiend.blackart2.games.rps.RPSClient $1 $2

