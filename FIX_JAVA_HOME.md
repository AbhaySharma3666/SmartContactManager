# Fix JAVA_HOME Issue

## Problem
JAVA_HOME is set incorrectly (includes \bin folder)

Current: `C:\Program Files\Java\jdk-21.0.10\bin`
Should be: `C:\Program Files\Java\jdk-21.0.10`

## Quick Solutions

### Option 1: Use Build Scripts (Easiest)
Run one of these scripts:
```bash
# Windows Command Prompt
build.bat

# PowerShell
.\build.ps1
```

### Option 2: Fix JAVA_HOME Permanently
1. Press `Win + X` → System
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Find JAVA_HOME in System Variables
5. Edit value to: `C:\Program Files\Java\jdk-21.0.10` (remove \bin)
6. Click OK
7. Restart PowerShell/CMD

### Option 3: Temporary Fix (Current Session)
```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
.\mvnw.cmd clean package
```

```cmd
# Command Prompt
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
mvnw.cmd clean package
```

### Option 4: Use Maven Directly (If installed)
```bash
mvn clean package
```

## Verify Fix
```bash
echo %JAVA_HOME%
# Should show: C:\Program Files\Java\jdk-21.0.10 (without \bin)
```
