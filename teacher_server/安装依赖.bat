@echo off
chcp 65001 >nul
title 安装 Python 依赖
echo ========================================
echo   安装 Python 依赖包
echo ========================================
echo.

REM 检查是否存在 requirements.txt
if not exist "requirements.txt" (
    echo 错误: 找不到 requirements.txt 文件！
    pause
    exit /b 1
)

echo 正在检查 Python 环境...
python --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Python！
    echo 请先安装 Python 3.8 或更高版本
    pause
    exit /b 1
)

echo Python 环境检查通过
echo.

echo 正在安装依赖包...
echo 这可能需要几分钟时间，请耐心等待...
echo.

python -m pip install --upgrade pip
python -m pip install -r requirements.txt

if errorlevel 1 (
    echo.
    echo ========================================
    echo   错误: 依赖包安装失败！
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. 网络连接问题
    echo 2. pip 版本过旧
    echo.
    echo 请检查错误信息并重试
    echo.
) else (
    echo.
    echo ========================================
    echo   依赖包安装完成！
    echo ========================================
    echo.
)

pause

