@echo off
rem ---------------------------------------------------------------------------
rem Stop script for Constellio
rem ---------------------------------------------------------------------------

set CONSTELLIO_HOME=%~dps0
set CATALINA_HOME=%CONSTELLIO_HOME%tomcat

rem If JRE_HOME is not defined, manually set JRE_HOME for Tomcat using the JVM
if "%JRE_HOME%"=="" for /f "delims=" %%A in ('java -classpath %CONSTELLIO_HOME% JreHomeProperty') do set JRE_HOME=%%A

echo Stopping Tomcat Application Server
call "%CATALINA_HOME%\bin\shutdown.bat"