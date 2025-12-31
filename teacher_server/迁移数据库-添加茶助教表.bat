@echo off
chcp 65001 >nul
echo ========================================
echo 茶文化课程 - 数据库迁移
echo 添加茶助教问答记录表
echo ========================================
echo.

python add_chat_messages_table.py

echo.
pause

