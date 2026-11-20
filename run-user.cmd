@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME (
  if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
  ) else (
    echo Set JAVA_HOME to a JDK 21 or newer before running this script.
    exit /b 1
  )
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
if exist ".tools\apache-maven-3.9.9\bin\mvn.cmd" (
  call ".tools\apache-maven-3.9.9\bin\mvn.cmd" -f src/chat-user/pom.xml compile exec:java
) else if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.6.3-bin\1iopthnavndlasol9gbrbg6bf2\apache-maven-3.6.3\bin\mvn.cmd" (
  call "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.6.3-bin\1iopthnavndlasol9gbrbg6bf2\apache-maven-3.6.3\bin\mvn.cmd" -f src/chat-user/pom.xml compile exec:java
) else (
  call mvn -f src/chat-user/pom.xml compile exec:java
)
