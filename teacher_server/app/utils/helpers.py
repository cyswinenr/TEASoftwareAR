"""
辅助函数
"""
from datetime import datetime

def format_datetime(dt):
    """格式化日期时间"""
    if not dt:
        return ''
    if isinstance(dt, str):
        return dt
    return dt.strftime('%Y-%m-%d %H:%M:%S')

def format_date(d):
    """格式化日期"""
    if not d:
        return ''
    if isinstance(d, str):
        return d
    return d.strftime('%Y-%m-%d')

