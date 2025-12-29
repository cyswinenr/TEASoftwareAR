@echo off
chcp 65001 >nul
title 初始化数据库
echo ========================================
echo   数据库初始化工具
echo ========================================
echo.

REM 检查是否存在初始化脚本
if exist "init_db.py" (
    echo 正在初始化数据库...
    echo.
    python init_db.py
    if errorlevel 1 (
        echo.
        echo ========================================
        echo   错误: 数据库初始化失败！
        echo ========================================
        echo.
        echo 可能的原因：
        echo 1. 未安装 Python 依赖包
        echo    解决方法: 运行 pip install -r requirements.txt
        echo.
        echo 2. Python 环境配置不正确
        echo    解决方法: 检查 Python 是否正确安装
        echo.
    ) else (
        echo.
        echo ========================================
        echo   数据库初始化完成！
        echo ========================================
    )
) else (
    echo 错误: 找不到 init_db.py 文件！
)

echo.
pause

