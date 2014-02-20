@echo off
rem ---------------------------------------------------------------------------
rem Start script for Constellio
rem ---------------------------------------------------------------------------

set CONSTELLIO_HOME=%~dps0
set CONSTELLIO_DATA=%CONSTELLIO_HOME%data
set CATALINA_HOME=%CONSTELLIO_HOME%tomcat

rem If JRE_HOME is not defined, manually set JRE_HOME for Tomcat using the JVM
if "%JRE_HOME%"=="" for /f "delims=" %%A in ('java -classpath %CONSTELLIO_HOME% JreHomeProperty') do set JRE_HOME=%%A

rem Set any default JVM options
set JAVA_OPTS=-Dfile.encoding=UTF8 -Xmx1024m -XX:MaxPermSize=160m -Dconstellio.home=%CONSTELLIO_HOME% -Dderby.system.home=%CONSTELLIO_DATA%

echo Starting Tomcat Application Server
call "%CATALINA_HOME%\bin\startup.bat"