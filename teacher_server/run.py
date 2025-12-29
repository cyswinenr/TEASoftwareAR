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
    # 开发环境运行
    app.run(
        host='0.0.0.0',
        port=8888,
        debug=True
    )

