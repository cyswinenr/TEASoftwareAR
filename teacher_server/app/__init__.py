"""
Flask应用初始化
"""
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
import os
from pathlib import Path

# 初始化扩展
db = SQLAlchemy()
cors = CORS()

def create_app(config_name=None):
    """应用工厂函数"""
    app = Flask(__name__)
    
    # 加载配置
    config_name = config_name or os.environ.get('FLASK_ENV', 'default')
    # 动态导入config模块（从项目根目录）
    import importlib.util
    config_path = Path(__file__).parent.parent / 'config.py'
    spec = importlib.util.spec_from_file_location("config", config_path)
    config_module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(config_module)
    app.config.from_object(config_module.config[config_name])
    
    # 确保上传目录存在
    upload_folder = app.config['UPLOAD_FOLDER']
    upload_folder.mkdir(parents=True, exist_ok=True)
    
    # 确保日志目录存在
    log_file = app.config['LOG_FILE']
    log_file.parent.mkdir(parents=True, exist_ok=True)
    
    # 初始化扩展
    db.init_app(app)
    cors.init_app(app, resources={r"/api/*": {"origins": "*"}})
    
    # 注册蓝图
    from app.routes.api import api_bp
    from app.routes.web import web_bp
    
    app.register_blueprint(api_bp, url_prefix='/api')
    app.register_blueprint(web_bp)
    
    # 创建数据库表
    with app.app_context():
        db.create_all()
    
    return app

