# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME is set to: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Build project
Write-Host "Building project..." -ForegroundColor Yellow
& .\mvnw.cmd clean package

Write-Host ""
Write-Host "Build complete!" -ForegroundColor Green
