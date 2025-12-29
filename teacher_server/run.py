"""
启动脚本
"""
import os
from app import create_app

# 获取环境变量
config_name = os.environ.get('FLASK_ENV', 'development')

# 创建应用
app = create_app(config_name)

if __name__ == '__main__':
    # 判断是否为打包后的可执行文件
    import sys
    if getattr(sys, 'frozen', False):
        # 打包后的可执行文件
        debug_mode = False
    else:
        # 开发环境
        debug_mode = True
    
    # 运行应用
    app.run(
        host='0.0.0.0',
        port=8888,
        debug=debug_mode
    )

