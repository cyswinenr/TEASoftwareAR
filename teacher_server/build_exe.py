"""
使用 PyInstaller 打包 Flask 应用为可执行文件
"""
import PyInstaller.__main__
import os
import sys

# 获取当前脚本所在目录
current_dir = os.path.dirname(os.path.abspath(__file__))

# PyInstaller 参数
args = [
    'run.py',                    # 主程序入口
    '--name=TeacherServer',      # 可执行文件名
    '--onefile',                 # 打包成单个文件
    '--windowed',                # Windows下不显示控制台窗口（如果需要看到日志，改为 --console）
    '--console',                 # 显示控制台窗口（方便查看日志）
    '--add-data', f'app;app',    # 包含 app 目录
    '--add-data', f'config.py;.', # 包含配置文件
    '--add-data', f'init_db.py;.', # 包含初始化脚本
    '--hidden-import=flask',     # 确保 Flask 被包含
    '--hidden-import=sqlalchemy',
    '--hidden-import=werkzeug',
    '--hidden-import=PIL',
    '--hidden-import=flask_cors',
    '--collect-all=flask',       # 收集 Flask 的所有数据文件
    '--collect-all=sqlalchemy',
    '--collect-all=werkzeug',
    '--clean',                   # 清理临时文件
    '--noconfirm',               # 覆盖输出目录而不询问
]

# 添加模板目录
templates_path = os.path.join(current_dir, 'app', 'templates')
if os.path.exists(templates_path):
    args.extend(['--add-data', f'{templates_path};app/templates'])

# 执行打包
print("开始打包...")
print(f"工作目录: {current_dir}")
print(f"打包参数: {' '.join(args)}")
print("\n")

PyInstaller.__main__.run(args)

print("\n打包完成！")
print(f"可执行文件位置: {current_dir}/dist/TeacherServer.exe")

