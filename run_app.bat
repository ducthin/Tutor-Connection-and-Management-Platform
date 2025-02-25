@echo off
echo Starting TCMP Application...

REM Set Java path if needed
REM set JAVA_HOME=C:\path\to\your\java

REM Run the Spring Boot application
if exist mvnw.cmd (
    call mvnw.cmd spring-boot:run
) else (
    echo Maven wrapper not found, trying the mvn command...
    mvn spring-boot:run
)

if %ERRORLEVEL% NEQ 0 (
    echo Application failed to start. See error messages above.
    pause
) else (
    echo Application is running...
)