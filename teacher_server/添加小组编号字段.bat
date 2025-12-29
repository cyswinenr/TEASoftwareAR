@echo off
chcp 65001 >nul
title 添加小组编号字段
echo ========================================
echo   添加小组编号字段到数据库
echo ========================================
echo.

REM 检查是否存在脚本
if exist "add_group_number_column.py" (
    echo 正在执行数据库更新...
    echo.
    python add_group_number_column.py
    if errorlevel 1 (
        echo.
        echo ========================================
        echo   错误: 数据库更新失败！
        echo ========================================
        echo.
        echo 可能的原因：
        echo 1. 数据库文件不存在
        echo 2. 数据库文件被占用（请先关闭服务器）
        echo 3. Python 环境配置不正确
        echo.
    )
) else (
    echo 错误: 找不到 add_group_number_column.py 文件！
)

echo.
pause

