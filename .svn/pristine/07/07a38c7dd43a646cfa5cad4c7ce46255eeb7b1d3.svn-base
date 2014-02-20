#!/bin/sh
# ---------------------------------------------------------------------------
# Start script for Constellio
# ---------------------------------------------------------------------------

export CONSTELLIO_HOME=`(cd -P $(dirname $0); pwd)`
export CONSTELLIO_DATA=$CONSTELLIO_HOME/data
export CATALINA_HOME=$CONSTELLIO_HOME/tomcat

chmod +x $CONSTELLIO_HOME/tomcat/bin/*.sh

# If JRE_HOME is not defined, manually set JRE_HOME for Tomcat using the JVM
if [ -f $JRE_HOME ]
then
 export JRE_HOME=`java -classpath $CONSTELLIO_HOME JreHomeProperty`
fi

# Set any default JVM options
export JAVA_OPTS="-Dfile.encoding=UTF8 -Xmx1024m -XX:MaxPermSize=160m -Dconstellio.home=$CONSTELLIO_HOME -Dderby.system.home=$CONSTELLIO_DATA"

echo Starting Tomcat Application Server
sh $CATALINA_HOME/bin/startup.sh

sleep 10