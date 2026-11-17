@echo off
setlocal
cd /d "%~dp0"
if not defined JAVA_HOME (
  echo Set JAVA_HOME to a JDK 21 or newer before running this script.
  exit /b 1
)
if exist ".tools\apache-maven-3.9.9\bin\mvn.cmd" (
  call ".tools\apache-maven-3.9.9\bin\mvn.cmd" -f src/chat-user/pom.xml compile exec:java
) else (
  call mvn -f src/chat-user/pom.xml compile exec:java
)
