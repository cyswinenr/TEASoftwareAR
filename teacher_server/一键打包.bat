@echo off
chcp 65001 >nul
title 一键打包教师端服务器
echo ========================================
echo   教师端服务器 - 一键打包工具
echo ========================================
echo.

REM 检查当前目录
cd /d "%~dp0"
echo 当前目录: %CD%
echo.

REM 检查是否存在 requirements.txt
if not exist "requirements.txt" (
    echo [错误] 找不到 requirements.txt 文件！
    echo 请确保在 teacher_server 目录下运行此脚本
    pause
    exit /b 1
)

REM 检查 Python 环境
echo [1/4] 检查 Python 环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python！
    echo 请先安装 Python 3.8 或更高版本
    pause
    exit /b 1
)
python --version
echo.

REM 检查并安装依赖
echo [2/4] 检查依赖包...
python -c "import PyInstaller" >nul 2>&1
if errorlevel 1 (
    echo 正在安装 PyInstaller...
    python -m pip install pyinstaller
    if errorlevel 1 (
        echo [错误] PyInstaller 安装失败！
        pause
        exit /b 1
    )
)

python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo 正在安装项目依赖...
    python -m pip install -r requirements.txt
    if errorlevel 1 (
        echo [错误] 依赖包安装失败！
        pause
        exit /b 1
    )
)
echo 依赖检查完成
echo.

REM 清理旧的打包文件
echo [3/4] 清理旧的打包文件...
if exist "build" (
    rmdir /s /q "build"
    echo 已清理 build 目录
)
if exist "dist" (
    rmdir /s /q "dist"
    echo 已清理 dist 目录
)
if exist "TeacherServer.spec" (
    del /q "TeacherServer.spec"
    echo 已清理旧的 spec 文件
)
echo.

REM 执行打包
echo [4/4] 开始打包...
echo 这可能需要几分钟时间，请耐心等待...
echo.

REM 使用 python -m PyInstaller 而不是直接调用 pyinstaller
python -m PyInstaller build.spec

if errorlevel 1 (
    echo.
    echo ========================================
    echo   [错误] 打包失败！
    echo ========================================
    echo.
    echo 请检查错误信息并重试
    echo.
    echo 如果问题持续，可以尝试手动执行：
    echo   python -m PyInstaller build.spec
    pause
    exit /b 1
)

echo.
echo ========================================
echo   [成功] 打包完成！
echo ========================================
echo.
echo 可执行文件位置: %CD%\dist\TeacherServer.exe
echo.
echo 下一步操作：
echo 1. 将 dist\TeacherServer.exe 复制到目标电脑
echo 2. 同时复制以下文件（如果需要）：
echo    - 启动服务器.bat
echo    - 安装说明.txt
echo.
echo 注意：打包后的 .exe 文件可能被杀毒软件误报，这是正常现象
echo.

pause

