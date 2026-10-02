@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set BASE_DIR=%~dp0
set WRAPPER_JAR="%BASE_DIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

if not exist %WRAPPER_JAR% (
  echo Downloading Maven Wrapper...
  powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar', '%BASE_DIR%.mvn\wrapper\maven-wrapper.jar')"
)

if not "%JAVA_HOME%"=="" goto valJavaHome
set JAVACMD=java
goto init

:valJavaHome
set JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVACMD%" goto init
echo Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
exit /b 1

:init
"%JAVACMD%" -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" %WRAPPER_LAUNCHER% %*

endlocal
