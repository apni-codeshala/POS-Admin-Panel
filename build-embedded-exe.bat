@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Embedded Self-Contained EXE Builder
echo ========================================

cd "C:\Users\adars\Desktop\POS-Admin-Panel"

REM Clean previous builds
echo [1/5] Cleaning previous builds...
if exist "target\embedded-output" rmdir /s /q "target\embedded-output"
if exist "target\embedded-runtime" rmdir /s /q "target\embedded-runtime"
if exist "target\embedded-input" rmdir /s /q "target\embedded-input"

REM Build JAR if needed
if not exist "target\AdminChronoPos-jar-with-dependencies.jar" (
    echo [2/5] Building JAR with dependencies...
    call mvnw.cmd clean package -DskipTests
) else (
    echo [2/5] JAR already exists, skipping build...
)

REM Prepare input for jpackage
echo [3/5] Preparing input files...
mkdir "target\embedded-input"
copy "target\AdminChronoPos-jar-with-dependencies.jar" "target\embedded-input\"

REM Create optimized runtime with JavaFX
echo [4/5] Creating optimized runtime with JavaFX...
set JAVAFX_JMODS_DIR=C:\Users\adars\Downloads\openjfx-17.0.16_windows-x64_bin-jmods\javafx-jmods-17.0.16

if not exist "!JAVAFX_JMODS_DIR!" (
    echo ERROR: JavaFX 17 jmods not found at: !JAVAFX_JMODS_DIR!
    echo Please verify the path exists
    pause
    exit /b 1
)

jlink ^
    --module-path "%JAVA_HOME%\jmods;!JAVAFX_JMODS_DIR!" ^
    --add-modules java.base,java.desktop,java.logging,java.sql,java.naming,java.management,java.security.jgss,java.xml,java.net.http,java.prefs,javafx.controls,javafx.fxml,javafx.base,javafx.graphics,jdk.crypto.ec,jdk.security.auth ^
    --output "target\embedded-runtime" ^
    --compress=2 ^
    --no-header-files ^
    --no-man-pages

if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to create runtime!
    pause
    exit /b 1
)

REM Create EMBEDDED EXE (single file with everything inside)
echo [5/5] Creating embedded single EXE file...
echo This will create a large EXE file with everything embedded...

jpackage ^
    --type exe ^
    --name "AdminChronoPosEmbedded" ^
    --app-version "1.0.0" ^
    --vendor "ChronoPos" ^
    --description "AdminChronoPos Self-Contained Application" ^
    --input "target\embedded-input" ^
    --main-jar "AdminChronoPos-jar-with-dependencies.jar" ^
    --main-class org.example.chronoadmin.AdminApplication ^
    --runtime-image "target\embedded-runtime" ^
    --dest "target\embedded-output" ^
    --win-console ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut

if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to create embedded EXE!
    pause
    exit /b 1
)

echo.
echo ========================================
echo           BUILD COMPLETE
echo ========================================

if exist "target\embedded-output\AdminChronoPosEmbedded-1.0.0.exe" (
    echo SUCCESS! Embedded EXE created at:
    echo target\embedded-output\AdminChronoPosEmbedded-1.0.0.exe
    echo.

    REM Check file size
    for %%A in ("target\embedded-output\AdminChronoPosEmbedded-1.0.0.exe") do (
        echo File size: %%~zA bytes
        set /a sizeMB=%%~zA/1048576
        echo File size: !sizeMB! MB
        echo.

        if !sizeMB! GTR 40 (
            echo ✓ SUCCESS: This is a truly embedded self-contained EXE!
            echo ✓ All runtime, JavaFX, and application files are embedded
            echo ✓ No external dependencies required
        ) else (
            echo ⚠ WARNING: Size seems small - may not be fully embedded
        )
    )

    echo.
    echo This EXE file contains:
    echo - Java runtime environment
    echo - JavaFX libraries
    echo - Your application JAR
    echo - All dependencies
    echo.
    echo You can distribute this single EXE file without any other files!

) else (
    echo ERROR: EXE file was not created successfully!
    echo Check the output above for errors.
)

echo.
echo Press any key to exit...
pause >nul
