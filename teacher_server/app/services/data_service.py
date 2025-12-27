"""
数据处理服务
"""
from datetime import datetime
from app import db
from app.models import (
    StudentGroup, GroupMember, Task1Data, Task2Data, 
    ThinkingQuestion, Photo
)
from app.utils.validators import *
from app.services.photo_service import save_photo_from_base64
from pathlib import Path
import os
import sys
# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent.parent))
from config import Config

def generate_submission_id(school, grade, class_number):
    """生成唯一提交ID"""
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    return f"{school}_{grade}_{class_number}_{timestamp}"

def save_student_group(data):
    """
    保存学生组数据
    
    Args:
        data: 包含学生信息的字典
    
    Returns:
        StudentGroup对象或None
    """
    try:
        student_info = data.get('studentInfo', {})
        
        # 验证数据
        if not validate_school(student_info.get('school')):
            return None
        if not validate_grade(student_info.get('grade')):
            return None
        if not validate_class_number(student_info.get('classNumber')):
            return None
        if not validate_date(student_info.get('date')):
            return None
        if not validate_member_count(student_info.get('memberCount', 0)):
            return None
        
        # 生成提交ID
        submission_id = generate_submission_id(
            student_info.get('school'),
            student_info.get('grade'),
            student_info.get('classNumber')
        )
        
        # 检查是否已存在
        existing = StudentGroup.query.filter_by(submission_id=submission_id).first()
        if existing:
            # 如果已存在，更新提交时间
            submission_id = f"{submission_id}_{datetime.now().strftime('%f')}"
        
        # 创建学生组
        group = StudentGroup(
            submission_id=submission_id,
            school=student_info.get('school'),
            grade=student_info.get('grade'),
            class_number=student_info.get('classNumber'),
            activity_date=datetime.strptime(student_info.get('date'), '%Y-%m-%d').date(),
            member_count=student_info.get('memberCount', 0)
        )
        
        db.session.add(group)
        db.session.flush()  # 获取group.id
        
        # 保存小组成员
        member_names = student_info.get('memberNames', [])
        for index, name in enumerate(member_names):
            if validate_member_name(name):
                member = GroupMember(
                    group_id=group.id,
                    member_index=index + 1,
                    member_name=name
                )
                db.session.add(member)
        
        return group
        
    except Exception as e:
        print(f"保存学生组失败: {e}")
        db.session.rollback()
        return None

def save_task1_data(group_id, data, submission_id):
    """保存任务一数据"""
    try:
        task1_data = data.get('task1', {})
        
        # 构建感官记录JSON
        sensory_records = {
            'dryTea': {
                'color': task1_data.get('dryTea', {}).get('color', ''),
                'aroma': task1_data.get('dryTea', {}).get('aroma', ''),
                'shape': task1_data.get('dryTea', {}).get('shape', ''),
                'taste': task1_data.get('dryTea', {}).get('taste', '')
            },
            'teaLiquor': {
                'color': task1_data.get('teaLiquor', {}).get('color', ''),
                'aroma': task1_data.get('teaLiquor', {}).get('aroma', ''),
                'shape': task1_data.get('teaLiquor', {}).get('shape', ''),
                'taste': task1_data.get('teaLiquor', {}).get('taste', '')
            },
            'spentLeaves': {
                'color': task1_data.get('spentLeaves', {}).get('color', ''),
                'aroma': task1_data.get('spentLeaves', {}).get('aroma', ''),
                'shape': task1_data.get('spentLeaves', {}).get('shape', ''),
                'taste': task1_data.get('spentLeaves', {}).get('taste', '')
            }
        }
        
        task1 = Task1Data(
            group_id=group_id,
            tea_name=task1_data.get('teaName', ''),
            teacher_tea_name=task1_data.get('teacherTeaName', ''),
            tea_category=task1_data.get('teaCategory', ''),
            water_temperature=task1_data.get('waterTemperature', ''),
            brewing_duration=task1_data.get('brewingDuration', ''),
            reflection_answer=task1_data.get('reflectionAnswer', '')
        )
        task1.set_sensory_records(sensory_records)
        
        db.session.add(task1)
        
        # 保存照片
        photos = task1_data.get('photos', [])
        for index, photo_base64 in enumerate(photos):
            if validate_photo_base64(photo_base64):
                save_photo_from_base64(
                    photo_base64, group_id, 'task1', index, submission_id
                )
        
        return task1
        
    except Exception as e:
        print(f"保存任务一数据失败: {e}")
        return None

def save_task2_data(group_id, data, submission_id):
    """保存任务二数据"""
    try:
        task2_data = data.get('task2', {})
        
        task2 = Task2Data(
            group_id=group_id,
            tea_name=task2_data.get('teaName', ''),
            water_temperature=task2_data.get('waterTemperature', ''),
            steeping_duration=task2_data.get('steepingDuration', ''),
            tea_color=task2_data.get('teaColor', ''),
            tea_aroma=task2_data.get('teaAroma', ''),
            tea_taste=task2_data.get('teaTaste', ''),
            meets_expectation=task2_data.get('meetsExpectation', False),
            not_meets_expectation=task2_data.get('notMeetsExpectation', False),
            reflection_answer=task2_data.get('reflectionAnswer', '')
        )
        
        db.session.add(task2)
        
        # 保存照片
        photos = task2_data.get('photos', [])
        for index, photo_base64 in enumerate(photos):
            if validate_photo_base64(photo_base64):
                save_photo_from_base64(
                    photo_base64, group_id, 'task2', index, submission_id
                )
        
        return task2
        
    except Exception as e:
        print(f"保存任务二数据失败: {e}")
        return None

def save_thinking_question(group_id, question_type, answer, photos, submission_id):
    """保存思考题数据"""
    try:
        thinking = ThinkingQuestion(
            group_id=group_id,
            question_type=question_type,
            answer=answer or ''
        )
        
        db.session.add(thinking)
        
        # 保存照片
        for index, photo_base64 in enumerate(photos):
            if validate_photo_base64(photo_base64):
                save_photo_from_base64(
                    photo_base64, group_id, question_type, index, submission_id
                )
        
        return thinking
        
    except Exception as e:
        print(f"保存思考题失败: {e}")
        return None

def save_all_data(data):
    """
    保存所有提交的数据
    
    Returns:
        (success: bool, message: str, submission_id: str)
    """
    try:
        # 保存学生组
        group = save_student_group(data)
        if not group:
            return False, "保存学生信息失败", None
        
        submission_id = group.submission_id
        
        # 保存任务一
        if data.get('task1'):
            save_task1_data(group.id, data, submission_id)
        
        # 保存任务二
        if data.get('task2'):
            save_task2_data(group.id, data, submission_id)
        
        # 保存思考题一
        if data.get('thinking1'):
            thinking1_data = data.get('thinking1', {})
            save_thinking_question(
                group.id, 'thinking1',
                thinking1_data.get('answer', ''),
                thinking1_data.get('photos', []),
                submission_id
            )
        
        # 保存思考题二
        if data.get('thinking2'):
            thinking2_data = data.get('thinking2', {})
            save_thinking_question(
                group.id, 'thinking2',
                thinking2_data.get('answer', ''),
                thinking2_data.get('photos', []),
                submission_id
            )
        
        # 保存创意题
        if data.get('creative'):
            creative_data = data.get('creative', {})
            save_thinking_question(
                group.id, 'creative',
                creative_data.get('answer', ''),
                creative_data.get('photos', []),
                submission_id
            )
        
        # 提交事务
        db.session.commit()
        
        return True, "数据保存成功", submission_id
        
    except Exception as e:
        db.session.rollback()
        print(f"保存数据失败: {e}")
        return False, f"保存数据失败: {str(e)}", None

def delete_student_data(submission_id):
    """
    删除学生提交的所有数据
    
    Args:
        submission_id: 提交ID
    
    Returns:
        (success: bool, message: str)
    """
    try:
        # 查找学生组
        group = StudentGroup.query.filter_by(submission_id=submission_id).first()
        
        if not group:
            return False, "学生数据不存在"
        
        # 删除所有照片文件
        for photo in group.photos:
            try:
                photo_path = Path(Config.UPLOAD_FOLDER) / Path(photo.file_path).name
                if photo_path.exists():
                    photo_path.unlink()
            except Exception as e:
                print(f"删除照片文件失败: {photo.file_path}, 错误: {e}")
        
        # 删除数据库记录（级联删除会自动删除关联数据）
        db.session.delete(group)
        db.session.commit()
        
        return True, "删除成功"
        
    except Exception as e:
        db.session.rollback()
        print(f"删除数据失败: {e}")
        return False, f"删除失败: {str(e)}"

