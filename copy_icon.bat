@echo off
chcp 65001 >nul
copy /Y "图标.png" "app\src\main\res\drawable\app_icon.png"
if %errorlevel%==0 (
    echo 图标文件复制成功！
) else (
    echo 复制失败，请手动将"图标.png"复制到"app\src\main\res\drawable\app_icon.png"
)
pause

