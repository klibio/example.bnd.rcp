@ECHO off
:: ###########################################################
:: -- Prepare the Command Processor --
SETLOCAL ENABLEEXTENSIONS
SETLOCAL ENABLEDELAYEDEXPANSION

:: ###########################################################################
:: cmd file for starting an pde build
:: ###########################################################################
SET "version=2.0.0"      &:20120809 is,pk  refactored the whole thing
:: ###########################################################################

:: -- Set the window title --
SET "title=%~nx0 - version %version%"
TITLE %title%

:: ###########################################################################
:: Validate the input parameters
:: ###########################################################################
SET PROPERTIES_FILE=%1
IF NOT EXIST %PROPERTIES_FILE% (
    ECHO The script will not start: path to localBuild_*.properties file is required. 
    ECHO Provided file %PROPERTIES_FILE% does not exist.
    EXIT
)

SET BUILD_SCRIPT_FILE=%2
IF "%BUILD_SCRIPT_FILE%"=="" (
    ECHO The script will not start: name of the build file to execute is missing 
    EXIT
)

SET BUILD_SCRIPT_PATH=%3
IF "%BUILD_SCRIPT_PATH%"=="" (
    ECHO The script will not start: path of the build file to execute is missing 
    EXIT
)
SET BUILD_SCRIPT_TARGET=%4
IF "%BUILD_SCRIPT_TARGET%"=="" (
    ECHO Not target specified: The script will run the default target
)

SET SCRIPT_PATH=%~dp0
SET SCRIPT_PATH=%SCRIPT_PATH:~0,-1%

:: - TIMESTAMP handling
SET X=
FOR /F "SKIP=1 DELIMS=" %%x IN ('WMIC OS GET localdatetime') DO IF NOT DEFINED X SET X=%%x
::echo.%X%

:: dissect into parts
SET DATE.YEAR=%X:~0,4%
SET DATE.MONTH=%X:~4,2%
SET DATE.DAY=%X:~6,2%
SET DATE.HOUR=%X:~8,2%
SET DATE.MINUTE=%X:~10,2%
SET DATE.SECOND=%X:~12,2%
SET DATE.FRACTIONS=%X:~15,6%
SET DATE.OFFSET=%X:~21,4%

SET DATEF=%DATE.YEAR%%DATE.MONTH%%DATE.DAY%
SET TIMEF=%DATE.HOUR%%DATE.MINUTE%%DATE.SECOND%
SET DATETIMEF=%DATEF%-%TIMEF%
ECHO using timestamp DATETIMEF=%DATETIMEF%


:: ###########################################################################
:: ###########################################################################
:: DO NO CHANGE BELOW
:: ###########################################################################
:: ###########################################################################

set "dir.workspace=%SCRIPT_PATH%/../../.."

:: MIND THE GAP - only existing variables can be used for replacements - order matters!

:: ###########################################################################
ECHO loading "general.local.properties" file into environment variables
:: ###########################################################################
SETLOCAL DISABLEDELAYEDEXPANSION
FOR /F "tokens=1,2 delims==" %%G IN (%dir.workspace%/build.cfg/general.local.properties) DO (
  echo.%%G | findstr /C:"#" 1> nul 2>&1
  if errorlevel 1 (
  SET "line=%%H"
  SETLOCAL ENABLEDELAYEDEXPANSION
  SET line=!line:${=%%!
  SET line=!line:}=%%!
::  ECHO SET %%G=!line!
  ECHO SET %%G=!line!>>"%TEMP%/env_vars.bat"
  ENDLOCAL
  ) 
)
ENDLOCAL

ECHO reading environment variables from %TEMP%/env_vars.bat 
CALL %TEMP%/env_vars.bat
::DEL /Q /F %TEMP%\env_vars.bat

SET LOCAL_BUILD_DIR=%ENGINE_WORK_DIR%/%buildDefinitionId%/%buildLabel%
SET CMD_LOGFILE=%LOCAL_BUILD_DIR%/.cmd/%DATETIMEF%_cmd.log
SET ECLIPSE_LOGFILE=%LOCAL_BUILD_DIR%/.cmd/%DATETIMEF%_eclipse.log
::SET

MKDIR "%LOCAL_BUILD_DIR%/.cmd" 1> nul 2>&1

echo ################################################################################ >> "%CMD_LOGFILE%" 
echo %DATETIMEF% starting eclipse application antRunner for pdeBuild with following command >> "%CMD_LOGFILE%"
echo. >> "%CMD_LOGFILE%"
echo.
ECHO. 
ECHO CD /D %LOCAL_BUILD_DIR%
ECHO "%JAVA8_DIR%/bin/java" ^^
ECHO -Xms40m -Xmx1024m ^^
ECHO -jar %ECLIPSE_BIN_DIR%/plugins/org.eclipse.equinox.launcher_%ECLIPSE_LAUNCHER_VERSION%.jar ^^
ECHO -application org.eclipse.ant.core.antRunner ^^
ECHO -configuration %LOCAL_BUILD_DIR%/.ecl/config ^^
ECHO -data %LOCAL_BUILD_DIR%/ecl/data ^^
ECHO -buildfile %BUILD_SCRIPT_PATH%/%BUILD_SCRIPT_FILE% ^^
ECHO -executionTargets %BUILD_SCRIPT_TARGET% ^^
ECHO -Djava.io.tmpdir=%LOCAL_BUILD_DIR%/TMP ^^
ECHO -Dbuilder=%BUILD_SCRIPT_PATH% ^^
ECHO -DENGINE_LIB_DIR=%ENGINE_LIB_DIR% ^^
ECHO -DBUILD_SCRIPT_FILE=%BUILD_SCRIPT_FILE% ^| "%SCRIPT_PATH%/tee.exe" "%CMD_LOGFILE%"

::  -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=y

CD /D %LOCAL_BUILD_DIR%
MKDIR %LOCAL_BUILD_DIR%\TMP

"%JAVA8_DIR%/bin/java" ^
-Xms40m -Xmx1024m  ^
-jar %ECLIPSE_BIN_DIR%/plugins/org.eclipse.equinox.launcher_%ECLIPSE_LAUNCHER_VERSION%.jar ^
-application org.eclipse.ant.core.antRunner ^
-configuration %LOCAL_BUILD_DIR%/.ecl/cfg ^
-data %LOCAL_BUILD_DIR%/.ecl/data ^
-buildfile %BUILD_SCRIPT_PATH%/%BUILD_SCRIPT_FILE% ^
-executionTargets %BUILD_SCRIPT_TARGET% ^
-Djava.io.tmpdir=%LOCAL_BUILD_DIR%/TMP ^
-Dbuilder=%BUILD_SCRIPT_PATH% ^
-DENGINE_LIB_DIR=%ENGINE_LIB_DIR% ^
-DBUILD_SCRIPT_FILE=%BUILD_SCRIPT_FILE% | "%SCRIPT_PATH%/tee.exe" "%CMD_LOGFILE%"

ECHO. >> "%CMD_LOGFILE%" 
ECHO COPY /Y "%LOCAL_BUILD_DIR%/.ecl/data/.metadata/.log" "%LOCAL_BUILD_DIR%/.cmd/%DATETIMEF%_eclipse.metadata.log" >> "%CMD_LOGFILE%"
IF EXISTS "%LOCAL_BUILD_DIR%/.ecl/data/.metadata/.log" {
   COPY /Y "%LOCAL_BUILD_DIR%/.ecl/data/.metadata/.log" "%LOCAL_BUILD_DIR%/.cmd/%DATETIMEF%_eclipse.metadata.log" >> "%CMD_LOGFILE%"
} 
{ECHO no log file found}

ECHO. >> "%CMD_LOGFILE%"  
ECHO end of build script reached >> "%CMD_LOGFILE%"
ECHO ################################################################################ >> "%CMD_LOGFILE%" 
