@echo off

echo ====================================
echo Book Trading System - Test DB Setup
echo Target: shopping_test
echo ====================================
echo.

if not exist "db\init_test.sql" (
    echo ERROR: db\init_test.sql not found
    pause
    exit /b 1
)

(
    echo -- ===== init_test.sql =====
    type "db\init_test.sql"
    echo.
    if exist "runtime_test_data.sql" (
        echo -- ===== runtime_test_data.sql =====
        type "runtime_test_data.sql"
    )
) > "%TEMP%\combined_setup_test.sql" 2>nul

echo Connecting to MySQL, enter root password...
mysql -u root -p < "%TEMP%\combined_setup_test.sql"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Failed! Check password/MySQL service/PATH
    del "%TEMP%\combined_setup_test.sql" 2>nul
    pause
    exit /b 1
)

del "%TEMP%\combined_setup_test.sql" 2>nul

echo.
if exist "runtime_test_data.sql" (
    echo Done! DB initialized successfully.
) else (
    echo Done (no runtime_test_data.sql, skipped data import)
)
pause