"""
初始化数据库
"""
import sys

try:
    from app import create_app, db
    from app.models import (
        StudentGroup, GroupMember, Task1Data, Task2Data,
        ThinkingQuestion, Photo
    )
except ImportError as e:
    print("=" * 50)
    print("错误: 缺少必要的 Python 模块！")
    print("=" * 50)
    print(f"错误详情: {e}")
    print()
    print("解决方法:")
    print("1. 确保已安装所有依赖包")
    print("   运行命令: pip install -r requirements.txt")
    print()
    print("2. 如果使用虚拟环境，请先激活虚拟环境")
    print()
    sys.exit(1)

def init_database():
    """初始化数据库表"""
    try:
        app = create_app('development')
        
        with app.app_context():
            # 删除所有表（谨慎使用）
            # db.drop_all()
            
            # 创建所有表
            db.create_all()
            
            print("=" * 50)
            print("数据库初始化完成！")
            print("=" * 50)
            print(f"数据库文件位置: {app.config['SQLALCHEMY_DATABASE_URI']}")
            return True
    except Exception as e:
        print("=" * 50)
        print("错误: 数据库初始化失败！")
        print("=" * 50)
        print(f"错误详情: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == '__main__':
    success = init_database()
    sys.exit(0 if success else 1)

