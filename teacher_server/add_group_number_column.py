"""
添加小组编号字段到数据库
"""
import sys
import sqlite3
from pathlib import Path

def add_group_number_column():
    """添加 group_number 字段到 student_groups 表"""
    try:
        # 数据库文件路径
        db_path = Path(__file__).parent / 'tea_culture.db'
        
        if not db_path.exists():
            print("=" * 50)
            print("错误: 找不到数据库文件！")
            print("=" * 50)
            print(f"预期路径: {db_path}")
            return False
        
        print("=" * 50)
        print("正在添加 group_number 字段...")
        print("=" * 50)
        print(f"数据库文件: {db_path}")
        print()
        
        # 连接数据库
        conn = sqlite3.connect(str(db_path))
        cursor = conn.cursor()
        
        # 检查字段是否已存在
        cursor.execute("PRAGMA table_info(student_groups)")
        columns = [column[1] for column in cursor.fetchall()]
        
        if 'group_number' in columns:
            print("提示: group_number 字段已存在，无需添加。")
            conn.close()
            return True
        
        # 添加字段
        print("正在执行 SQL: ALTER TABLE student_groups ADD COLUMN group_number INTEGER;")
        cursor.execute("ALTER TABLE student_groups ADD COLUMN group_number INTEGER")
        
        # 提交更改
        conn.commit()
        
        # 验证字段是否添加成功
        cursor.execute("PRAGMA table_info(student_groups)")
        columns = [column[1] for column in cursor.fetchall()]
        
        if 'group_number' in columns:
            print()
            print("=" * 50)
            print("✓ 成功添加 group_number 字段！")
            print("=" * 50)
            
            # 统计现有记录数
            cursor.execute("SELECT COUNT(*) FROM student_groups")
            count = cursor.fetchone()[0]
            print(f"当前数据库中有 {count} 条学生组记录")
            print()
            print("注意: 现有记录的 group_number 字段值为 NULL（空）")
            print("      新提交的数据会自动包含小组编号")
            
            conn.close()
            return True
        else:
            print()
            print("=" * 50)
            print("错误: 字段添加失败！")
            print("=" * 50)
            conn.close()
            return False
            
    except sqlite3.OperationalError as e:
        print()
        print("=" * 50)
        print("错误: SQL 执行失败！")
        print("=" * 50)
        print(f"错误详情: {e}")
        if conn:
            conn.close()
        return False
    except Exception as e:
        print()
        print("=" * 50)
        print("错误: 操作失败！")
        print("=" * 50)
        print(f"错误详情: {e}")
        import traceback
        traceback.print_exc()
        if conn:
            conn.close()
        return False

if __name__ == '__main__':
    success = add_group_number_column()
    print()
    if success:
        print("操作完成！")
    else:
        print("操作失败，请检查错误信息。")
    print()
    input("按 Enter 键退出...")
    sys.exit(0 if success else 1)

