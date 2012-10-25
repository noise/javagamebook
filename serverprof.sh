export CLASSPATH=.:./tmpclasses:lib/log4j-1.2.6.jar:$CLASSPATH
echo $CLASSPATH
java -cp $CLASSPATH -verbose:gc -server com.hypefiend.blackart.server.GameServer
#java -cp $CLASSPATH -verbose:gc -Xrunhprof:cpu=times com.hypefiend.blackart.server.GameServer
