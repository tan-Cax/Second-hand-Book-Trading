@echo off

echo ====================================
echo Book Trading System - DB Setup
echo Target: shopping_db
echo ====================================
echo.

if not exist "db\init.sql" (
    echo ERROR: db\init.sql not found
    pause
    exit /b 1
)

(
    echo -- ===== init.sql =====
    type "db\init.sql"
    echo.
    if exist "runtime_data.sql" (
        echo -- ===== runtime_data.sql =====
        type "runtime_data.sql"
    )
) > "%TEMP%\combined_setup.sql" 2>nul

echo Connecting to MySQL, enter root password...
mysql -u root -p < "%TEMP%\combined_setup.sql"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Failed! Check password/MySQL service/PATH
    del "%TEMP%\combined_setup.sql" 2>nul
    pause
    exit /b 1
)

del "%TEMP%\combined_setup.sql" 2>nul

echo.
if exist "runtime_data.sql" (
    echo Done! DB initialized successfully.
) else (
    echo Done (no runtime_data.sql, skipped data import)
)
pause