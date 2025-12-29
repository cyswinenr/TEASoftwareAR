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
import hashlib
import random
# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent.parent))
from config import Config

def generate_group_code(school, grade, class_number, member_names):
    """
    生成固定的组标识码，用于识别同一组学生
    
    Args:
        school: 学校名称
        grade: 年级
        class_number: 班级
        member_names: 成员姓名列表（已排序）
    
    Returns:
        group_code字符串（固定值，不包含时间戳）
    """
    # 成员信息哈希（取前12位，确保唯一性）
    members_str = '_'.join(sorted(member_names))  # 排序确保一致性
    members_hash = hashlib.md5(members_str.encode('utf-8')).hexdigest()[:12]
    
    # 组合：学校_年级_班级_成员哈希
    group_code = f"{school}_{grade}_{class_number}_{members_hash}"
    
    return group_code

def generate_submission_id(group_code, activity_date):
    """
    生成提交ID（用于显示和标识单次提交）
    
    Args:
        group_code: 组标识码
        activity_date: 活动日期
        timestamp: 时间戳（可选，用于区分同组的不同提交）
    
    Returns:
        submission_id字符串
    """
    # 活动日期
    if isinstance(activity_date, str):
        date_str = activity_date.replace('-', '')
    else:
        date_str = activity_date.strftime('%Y%m%d')
    
    # 时间戳（精确到微秒）
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    
    # 组合：组标识码_日期_时间戳
    submission_id = f"{group_code}_{date_str}_{timestamp}"
    
    return submission_id

def find_or_create_student_group(data, submission_id=None):
    """
    查找或创建学生组，带完整验证逻辑
    
    Args:
        data: 包含学生信息的字典
        submission_id: 如果提供，则查找并验证现有记录；否则创建新记录
    
    Returns:
        (StudentGroup对象或None, 错误消息字符串)
    """
    try:
        student_info = data.get('studentInfo', {})
        
        # 验证数据
        grade_value = student_info.get('grade', '').strip()
        if not validate_school(student_info.get('school')):
            return None, "学校信息无效"
        if not validate_grade(grade_value):
            if not grade_value:
                return None, "年级信息无效：请选择年级（高一或高二）"
            else:
                return None, f"年级信息无效：'{grade_value}' 不是有效的年级，请选择'高一'或'高二'"
        if not validate_class_number(student_info.get('classNumber')):
            return None, "班级信息无效"
        if not validate_date(student_info.get('date')):
            return None, "日期信息无效"
        if not validate_member_count(student_info.get('memberCount', 0)):
            return None, "成员数量无效"
        
        activity_date = datetime.strptime(student_info.get('date'), '%Y-%m-%d').date()
        member_names = sorted([name for name in student_info.get('memberNames', []) if validate_member_name(name)])
        
        if not member_names:
            return None, "成员姓名列表为空或无效"
        
        school = student_info.get('school')
        grade = student_info.get('grade')
        class_number = student_info.get('classNumber')
        group_number = student_info.get('groupNumber', 0)  # 获取小组编号
        
        # 生成固定的组标识码
        group_code = generate_group_code(school, grade, class_number, member_names)
        
        # 如果提供了 submission_id，先尝试通过 submission_id 查找
        if submission_id:
            group = StudentGroup.query.filter_by(submission_id=submission_id).first()
            if group:
                # 验证是否为同一组学生（通过组标识码）
                existing_group_code = generate_group_code(
                    group.school, group.grade, group.class_number,
                    sorted([m.member_name for m in group.members])
                )
                
                if existing_group_code == group_code:
                    # 验证通过，更新记录
                    group.submit_time = datetime.utcnow()
                    group.updated_at = datetime.utcnow()
                    group.member_count = len(member_names)
                    if group_number > 0:
                        group.group_number = group_number
                    
                    # 更新成员信息
                    GroupMember.query.filter_by(group_id=group.id).delete()
                    for index, name in enumerate(member_names):
                        member = GroupMember(
                            group_id=group.id,
                            member_index=index + 1,
                            member_name=name
                        )
                        db.session.add(member)
                    
                    return group, "更新成功"
                else:
                    return None, f"submission_id 对应的组信息不匹配。请检查是否为同一组学生。"
            else:
                return None, f"找不到 submission_id: {submission_id}。请检查ID是否正确，或创建新提交（不提供 submission_id）。"
        
        # 没有提供 submission_id，根据组标识码查找现有记录
        # 查找相同学校、年级、班级、活动日期、成员信息的记录
        potential_groups = StudentGroup.query.filter(
            StudentGroup.school == school,
            StudentGroup.grade == grade,
            StudentGroup.class_number == class_number,
            StudentGroup.activity_date == activity_date
        ).all()
        
        # 检查成员信息是否匹配
        for potential_group in potential_groups:
            existing_members = sorted([m.member_name for m in potential_group.members])
            if existing_members == member_names:
                # 找到匹配的记录，更新提交时间并返回
                potential_group.submit_time = datetime.utcnow()
                potential_group.updated_at = datetime.utcnow()
                print(f"[组匹配] ✓ 找到现有记录: {potential_group.submission_id}, 更新提交时间")
                return potential_group, "找到现有记录并更新"
        
        # 没有找到匹配的记录，创建新记录
        new_submission_id = generate_submission_id(group_code, activity_date)
        
        # 检查是否已存在（极小概率，但为了安全）
        existing = StudentGroup.query.filter_by(submission_id=new_submission_id).first()
        if existing:
            # 如果已存在，添加随机数
            new_submission_id = f"{new_submission_id}_{random.randint(1000, 9999)}"
        
        # 创建学生组
        group = StudentGroup(
            submission_id=new_submission_id,
            school=school,
            grade=grade,
            class_number=class_number,
            activity_date=activity_date,
            member_count=len(member_names),
            group_number=group_number if group_number > 0 else None
        )
        
        db.session.add(group)
        db.session.flush()  # 获取group.id
        
        # 保存小组成员
        for index, name in enumerate(member_names):
            member = GroupMember(
                group_id=group.id,
                member_index=index + 1,
                member_name=name
            )
            db.session.add(member)
        
        print(f"[新建记录] group_code: {group_code}, submission_id: {new_submission_id}")
        return group, "创建成功"
        
    except Exception as e:
        print(f"保存学生组失败: {e}")
        import traceback
        traceback.print_exc()
        db.session.rollback()
        return None, f"保存失败: {str(e)}"

def save_task1_data(group_id, data, submission_id, update_existing=False):
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
        
        # 如果更新现有记录，先查找
        if update_existing:
            existing_task1 = Task1Data.query.filter_by(group_id=group_id).first()
            if existing_task1:
                # 更新现有记录
                existing_task1.tea_name = task1_data.get('teaName', '')
                existing_task1.teacher_tea_name = task1_data.get('teacherTeaName', '')
                existing_task1.tea_category = task1_data.get('teaCategory', '')
                existing_task1.water_temperature = task1_data.get('waterTemperature', '')
                existing_task1.brewing_duration = task1_data.get('brewingDuration', '')
                existing_task1.reflection_answer = task1_data.get('reflectionAnswer', '')
                existing_task1.set_sensory_records(sensory_records)
                
                # 删除旧照片
                Photo.query.filter_by(group_id=group_id, photo_type='task1').delete()
                
                # 保存新照片
                photos = task1_data.get('photos', [])
                for index, photo_base64 in enumerate(photos):
                    if validate_photo_base64(photo_base64):
                        save_photo_from_base64(
                            photo_base64, group_id, 'task1', index, submission_id
                        )
                
                return existing_task1
        
        # 创建新记录
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
        import traceback
        traceback.print_exc()
        return None

def save_task2_data(group_id, data, submission_id, update_existing=False):
    """保存任务二数据"""
    try:
        task2_data = data.get('task2', {})
        
        # 如果更新现有记录，先查找
        if update_existing:
            existing_task2 = Task2Data.query.filter_by(group_id=group_id).first()
            if existing_task2:
                # 更新现有记录
                existing_task2.tea_name = task2_data.get('teaName', '')
                existing_task2.water_temperature = task2_data.get('waterTemperature', '')
                existing_task2.steeping_duration = task2_data.get('steepingDuration', '')
                existing_task2.tea_color = task2_data.get('teaColor', '')
                existing_task2.tea_aroma = task2_data.get('teaAroma', '')
                existing_task2.tea_taste = task2_data.get('teaTaste', '')
                existing_task2.meets_expectation = task2_data.get('meetsExpectation', False)
                existing_task2.not_meets_expectation = task2_data.get('notMeetsExpectation', False)
                existing_task2.reflection_answer = task2_data.get('reflectionAnswer', '')
                
                # 删除旧照片
                Photo.query.filter_by(group_id=group_id, photo_type='task2').delete()
                
                # 保存新照片
                photos = task2_data.get('photos', [])
                for index, photo_base64 in enumerate(photos):
                    if validate_photo_base64(photo_base64):
                        save_photo_from_base64(
                            photo_base64, group_id, 'task2', index, submission_id
                        )
                
                return existing_task2
        
        # 创建新记录
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
        import traceback
        traceback.print_exc()
        return None

def save_thinking_question(group_id, question_type, answer, photos, submission_id, update_existing=False):
    """保存思考题数据"""
    try:
        # 如果更新现有记录，先查找
        if update_existing:
            existing_thinking = ThinkingQuestion.query.filter_by(
                group_id=group_id, 
                question_type=question_type
            ).first()
            if existing_thinking:
                # 更新现有记录
                existing_thinking.answer = answer or ''
                
                # 删除旧照片
                Photo.query.filter_by(group_id=group_id, photo_type=question_type).delete()
                
                # 保存新照片
                for index, photo_base64 in enumerate(photos):
                    if validate_photo_base64(photo_base64):
                        save_photo_from_base64(
                            photo_base64, group_id, question_type, index, submission_id
                        )
                
                return existing_thinking
        
        # 创建新记录
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
        import traceback
        traceback.print_exc()
        return None

def save_all_data(data, submission_id=None):
    """
    保存所有提交的数据，支持更新现有记录
    
    Args:
        data: 提交的数据
        submission_id: 如果提供，则更新现有记录；否则创建新记录
    
    Returns:
        (success: bool, message: str, submission_id: str)
    """
    try:
        # 查找或创建学生组（带完整验证）
        group, message = find_or_create_student_group(data, submission_id)
        
        if not group:
            return False, message, None
        
        result_submission_id = group.submission_id
        # 判断是否为更新：提供了 submission_id 或者智能匹配到了现有记录
        is_update = submission_id is not None or message == "找到现有记录并更新"
        
        # 记录日志
        if submission_id is not None:
            print(f"[更新-提供ID] submission_id: {result_submission_id}")
        elif message == "找到现有记录并更新":
            print(f"[更新-智能匹配] submission_id: {result_submission_id}")
        else:
            print(f"[新建] submission_id: {result_submission_id}")
        
        # 保存任务一
        if data.get('task1'):
            save_task1_data(group.id, data, result_submission_id, update_existing=is_update)
        
        # 保存任务二
        if data.get('task2'):
            save_task2_data(group.id, data, result_submission_id, update_existing=is_update)
        
        # 保存思考题一
        if data.get('thinking1'):
            thinking1_data = data.get('thinking1', {})
            save_thinking_question(
                group.id, 'thinking1',
                thinking1_data.get('answer', ''),
                thinking1_data.get('photos', []),
                result_submission_id,
                update_existing=is_update
            )
        
        # 保存思考题二
        if data.get('thinking2'):
            thinking2_data = data.get('thinking2', {})
            save_thinking_question(
                group.id, 'thinking2',
                thinking2_data.get('answer', ''),
                thinking2_data.get('photos', []),
                result_submission_id,
                update_existing=is_update
            )
        
        # 保存创意题
        if data.get('creative'):
            creative_data = data.get('creative', {})
            save_thinking_question(
                group.id, 'creative',
                creative_data.get('answer', ''),
                creative_data.get('photos', []),
                result_submission_id,
                update_existing=is_update
            )
        
        # 提交事务
        db.session.commit()
        
        final_message = "数据更新成功" if is_update else "数据保存成功"
        return True, final_message, result_submission_id
        
    except Exception as e:
        db.session.rollback()
        print(f"保存数据失败: {e}")
        import traceback
        traceback.print_exc()
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

