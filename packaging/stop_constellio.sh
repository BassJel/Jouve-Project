#!/bin/sh
# ---------------------------------------------------------------------------
# Start script for Constellio
# ---------------------------------------------------------------------------
export CONSTELLIO_HOME=`(cd -P $(dirname $0); pwd)`
export CONSTELLIO_DATA=$CONSTELLIO_HOME/data
export CATALINA_HOME=$CONSTELLIO_HOME/tomcat

# If JRE_HOME is not defined, manually set JRE_HOME for Tomcat using the JVM
if [ -f $JRE_HOME ]
then
 export JRE_HOME=`java -classpath $CONSTELLIO_HOME JreHomeProperty`
fi

echo Stopping Tomcat Application Server
sh $CATALINA_HOME/bin/shutdown.sh

sleep 10