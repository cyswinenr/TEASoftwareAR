"""
数据验证工具
"""
import re
from datetime import datetime

def validate_submission_id(submission_id):
    """验证提交ID格式"""
    if not submission_id or len(submission_id) > 64:
        return False
    return True

def validate_school(school):
    """验证学校名称"""
    if not school or len(school) > 100:
        return False
    return True

def validate_grade(grade):
    """验证年级"""
    valid_grades = ['高一', '高二']
    return grade in valid_grades

def validate_class_number(class_number):
    """验证班级号"""
    if not class_number:
        return False
    return class_number.isdigit()

def validate_date(date_str):
    """验证日期格式"""
    try:
        datetime.strptime(date_str, '%Y-%m-%d')
        return True
    except:
        return False

def validate_member_count(count):
    """验证成员人数"""
    return isinstance(count, int) and 1 <= count <= 10

def validate_member_name(name):
    """验证成员姓名"""
    if not name or len(name) > 50:
        return False
    # 检查是否包含中文字符
    if not re.search(r'[\u4e00-\u9fa5]', name):
        return False
    return True

def validate_photo_base64(base64_str):
    """验证Base64编码的照片"""
    if not base64_str:
        return False
    # 简单的Base64格式检查
    try:
        import base64
        base64.b64decode(base64_str)
        return True
    except:
        return False

