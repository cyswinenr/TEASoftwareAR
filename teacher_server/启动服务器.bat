@echo off
chcp 65001 >nul
title 教师端服务器
echo ========================================
echo   茶文化课程APP - 教师端服务器
echo ========================================
echo.
echo 正在启动服务器...
echo 服务器地址: http://localhost:8888
echo.
echo 按 Ctrl+C 可以停止服务器
echo ========================================
echo.

REM 检查是否存在可执行文件
if exist "TeacherServer.exe" (
    TeacherServer.exe
) else if exist "run.py" (
    python run.py
) else (
    echo 错误: 找不到启动文件！
    echo 请确保 TeacherServer.exe 或 run.py 存在。
    pause
    exit /b 1
)

pause

