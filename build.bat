@echo off
echo Setting JAVA_HOME...
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME is set to: %JAVA_HOME%
echo.

echo Building project...
call mvnw.cmd clean package

echo.
echo Build complete!
pause
