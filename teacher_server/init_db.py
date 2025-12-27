"""
初始化数据库
"""
from app import create_app, db
from app.models import (
    StudentGroup, GroupMember, Task1Data, Task2Data,
    ThinkingQuestion, Photo
)

def init_database():
    """初始化数据库表"""
    app = create_app('development')
    
    with app.app_context():
        # 删除所有表（谨慎使用）
        # db.drop_all()
        
        # 创建所有表
        db.create_all()
        
        print("数据库初始化完成！")
        print(f"数据库文件位置: {app.config['SQLALCHEMY_DATABASE_URI']}")

if __name__ == '__main__':
    init_database()

