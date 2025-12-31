@echo off
chcp 65001 >nul
echo ========================================
echo Installing Export Dependencies
echo ========================================
echo.
echo Installing required libraries...
echo.

pip install openpyxl==3.1.2 reportlab==4.0.7

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo Installation completed successfully!
    echo You can now use export features.
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Installation failed! Please check the error messages above.
    echo ========================================
)
echo.
pause

