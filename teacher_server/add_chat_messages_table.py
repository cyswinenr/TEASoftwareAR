"""
添加茶助教问答记录表（chat_messages）的数据库迁移脚本

运行方法：
    python add_chat_messages_table.py

注意：如果数据库表已存在，脚本会自动跳过创建
"""
import sys
from pathlib import Path

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent))

from app import create_app, db
from app.models import ChatMessage

def migrate_database():
    """执行数据库迁移"""
    app = create_app()
    
    with app.app_context():
        try:
            # 检查表是否已存在
            from sqlalchemy import inspect
            inspector = inspect(db.engine)
            tables = inspector.get_table_names()
            
            if 'chat_messages' in tables:
                print("✓ 表 chat_messages 已存在，跳过创建")
                return True
            
            # 创建表
            print("正在创建表 chat_messages...")
            db.create_all()
            print("✓ 表 chat_messages 创建成功！")
            
            # 验证表是否创建成功
            inspector = inspect(db.engine)
            tables = inspector.get_table_names()
            
            if 'chat_messages' in tables:
                print("✓ 数据库迁移完成！")
                
                # 显示表结构
                columns = inspector.get_columns('chat_messages')
                print("\n表结构:")
                print("-" * 60)
                for col in columns:
                    print(f"  {col['name']:<20} {col['type']}")
                print("-" * 60)
                
                return True
            else:
                print("✗ 表创建失败，请检查错误信息")
                return False
                
        except Exception as e:
            print(f"✗ 数据库迁移失败: {e}")
            import traceback
            traceback.print_exc()
            return False

if __name__ == '__main__':
    print("=" * 60)
    print("茶文化课程 - 数据库迁移脚本")
    print("添加茶助教问答记录表")
    print("=" * 60)
    print()
    
    success = migrate_database()
    
    print()
    if success:
        print("✓ 迁移完成！服务器现在可以接收和存储茶助教问答记录。")
    else:
        print("✗ 迁移失败，请检查错误信息并重试。")
    print()

