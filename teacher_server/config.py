"""
配置文件
支持开发和生产环境
"""
import os
from pathlib import Path

# 项目根目录
BASE_DIR = Path(__file__).parent

# 环境配置
class Config:
    """基础配置"""
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'dev-secret-key-change-in-production'
    
    # 数据库配置
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_RECORD_QUERIES = True
    
    # 文件上传配置
    UPLOAD_FOLDER = BASE_DIR / 'uploads' / 'photos'
    MAX_CONTENT_LENGTH = 50 * 1024 * 1024  # 50MB
    ALLOWED_EXTENSIONS = {'jpg', 'jpeg', 'png', 'gif'}
    
    # 分页配置
    ITEMS_PER_PAGE = 20
    
    # 日志配置
    LOG_FILE = BASE_DIR / 'logs' / 'app.log'
    LOG_LEVEL = 'INFO'

class DevelopmentConfig(Config):
    """开发环境配置"""
    DEBUG = True
    SQLALCHEMY_DATABASE_URI = os.environ.get('DEV_DATABASE_URL') or \
        f'sqlite:///{BASE_DIR / "tea_culture.db"}'
    
class ProductionConfig(Config):
    """生产环境配置"""
    DEBUG = False
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
        f'sqlite:///{BASE_DIR / "tea_culture.db"}'
    
    # 生产环境建议使用PostgreSQL
    # SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
    #     'postgresql://user:password@localhost/tea_culture'

# 配置字典
config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'default': DevelopmentConfig
}

