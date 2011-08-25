@echo off

::----------------------------------------------------------------------
:: Flex IDE Startup Script
::----------------------------------------------------------------------

:: ---------------------------------------------------------------------
:: Before you run Flex IDE specify the location of the
:: JDK 1.5 installation directory which will be used for running Flex IDE
:: ---------------------------------------------------------------------
IF "%FLEXIDE_JDK%" == "" SET FLEXIDE_JDK=%JDK_HOME%
IF "%FLEXIDE_JDK%" == "" goto error

:: ---------------------------------------------------------------------
:: Before you run Flex IDE specify the location of the
:: directory where Flex IDE is installed
:: In most cases you do not need to change the settings below.
:: ---------------------------------------------------------------------
SET FLEXIDE_HOME=..

SET JAVA_EXE=%FLEXIDE_JDK%\jre\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" goto error

IF "%FLEXIDE_MAIN_CLASS_NAME%" == "" SET FLEXIDE_MAIN_CLASS_NAME=com.intellij.idea.Main

IF NOT "%FLEXIDE_PROPERTIES%" == "" set FLEXIDE_PROPERTIES_PROPERTY="-Didea.properties.file=%FLEXIDE_PROPERTIES%"

:: ---------------------------------------------------------------------
:: You may specify your own JVM arguments in flexide.exe.vmoptions file. Put one option per line there.
:: ---------------------------------------------------------------------
SET ACC=
FOR /F "delims=" %%i in (%FLEXIDE_HOME%\bin\flexide.exe.vmoptions) DO call %FLEXIDE_HOME%\bin\append.bat "%%i"

set REQUIRED_FLEXIDE_JVM_ARGS=-Xbootclasspath/a:%FLEXIDE_HOME%/lib/boot.jar -Didea.platform.prefix=Flex -Didea.no.jre.check=true -Didea.paths.selector=@@system_selector@@ %FLEXIDE_PROPERTIES_PROPERTY% %REQUIRED_FLEXIDE_JVM_ARGS%
SET JVM_ARGS=%ACC% %REQUIRED_FLEXIDE_JVM_ARGS%

SET OLD_PATH=%PATH%
SET PATH=%FLEXIDE_HOME%\bin;%PATH%

SET CLASS_PATH=%FLEXIDE_HOME%\lib\bootstrap.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\util.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\jdom.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\log4j.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\extensions.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\trove4j.jar
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_HOME%\lib\jna.jar

:: TODO[yole]: remove
SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_JDK%\lib\tools.jar

:: ---------------------------------------------------------------------
:: You may specify additional class paths in FLEXIDE_CLASS_PATH variable.
:: It is a good idea to specify paths to your plugins in this variable.
:: ---------------------------------------------------------------------
IF NOT "%FLEXIDE_CLASS_PATH%" == "" SET CLASS_PATH=%CLASS_PATH%;%FLEXIDE_CLASS_PATH%

"%JAVA_EXE%" %JVM_ARGS% -cp "%CLASS_PATH%" %FLEXIDE_MAIN_CLASS_NAME% %*

SET PATH=%OLD_PATH%
goto end
:error
echo ---------------------------------------------------------------------
echo ERROR: cannot start Flex IDE.
echo No JDK found to run Flex IDE. Please validate either FLEXIDE_JDK or JDK_HOME points to valid JDK installation
echo ---------------------------------------------------------------------
pause
:end
