"""
Web路由 - 教师查看界面
"""
from flask import Blueprint, render_template, request, jsonify, send_from_directory, redirect, url_for
from app import db
from app.models import StudentGroup, GroupMember, Task1Data, Task2Data, ThinkingQuestion, Photo, ChatMessage
from app.services.data_service import delete_student_data
from pathlib import Path
from sqlalchemy import case
import sys
# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent.parent))
from config import Config

web_bp = Blueprint('web', __name__)

@web_bp.route('/')
def index():
    """学生列表页"""
    from datetime import datetime
    
    # 获取查询参数
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', Config.ITEMS_PER_PAGE, type=int)
    school = request.args.get('school', '')
    grade = request.args.get('grade', '')
    class_number = request.args.get('class_number', '')
    start_date = request.args.get('start_date', '')
    end_date = request.args.get('end_date', '')
    
    # 构建查询，使用 eager loading 避免 N+1 查询问题
    from sqlalchemy.orm import joinedload
    query = StudentGroup.query.options(
        joinedload(StudentGroup.task1),
        joinedload(StudentGroup.task2),
        joinedload(StudentGroup.thinking_questions)
    )
    
    if school:
        query = query.filter(StudentGroup.school.contains(school))
    if grade:
        query = query.filter(StudentGroup.grade == grade)
    if class_number:
        query = query.filter(StudentGroup.class_number == class_number)
    
    # 按提交时间筛选
    if start_date:
        try:
            start_datetime = datetime.strptime(start_date, '%Y-%m-%d')
            query = query.filter(StudentGroup.submit_time >= start_datetime)
        except ValueError:
            pass  # 忽略无效的日期格式
    
    if end_date:
        try:
            # 结束日期包含整天，所以设置为当天的23:59:59
            end_datetime = datetime.strptime(end_date, '%Y-%m-%d')
            end_datetime = end_datetime.replace(hour=23, minute=59, second=59)
            query = query.filter(StudentGroup.submit_time <= end_datetime)
        except ValueError:
            pass  # 忽略无效的日期格式
    
    # 按小组编号升序，然后按提交时间倒序
    # 使用case语句将NULL值排在最后（设置为999999）
    query = query.order_by(
        case((StudentGroup.group_number.is_(None), 999999), else_=StudentGroup.group_number).asc(),
        StudentGroup.submit_time.desc()
    )
    
    # 分页
    pagination = query.paginate(
        page=page,
        per_page=per_page,
        error_out=False
    )
    
    return render_template('index.html', 
                         pagination=pagination,
                         students=pagination.items,
                         school=school,
                         grade=grade,
                         class_number=class_number,
                         start_date=start_date,
                         end_date=end_date)

@web_bp.route('/student/<submission_id>')
def student_detail(submission_id):
    """学生详情页"""
    group = StudentGroup.query.filter_by(submission_id=submission_id).first_or_404()
    
    return render_template('student_detail.html', group=group)

@web_bp.route('/api/students')
def api_students():
    """API: 获取学生列表"""
    from datetime import datetime
    
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('limit', Config.ITEMS_PER_PAGE, type=int)
    school = request.args.get('school', '')
    grade = request.args.get('grade', '')
    class_number = request.args.get('class_number', '')
    start_date = request.args.get('start_date', '')
    end_date = request.args.get('end_date', '')
    
    query = StudentGroup.query
    
    if school:
        query = query.filter(StudentGroup.school.contains(school))
    if grade:
        query = query.filter(StudentGroup.grade == grade)
    if class_number:
        query = query.filter(StudentGroup.class_number == class_number)
    
    # 按提交时间筛选
    if start_date:
        try:
            start_datetime = datetime.strptime(start_date, '%Y-%m-%d')
            query = query.filter(StudentGroup.submit_time >= start_datetime)
        except ValueError:
            pass  # 忽略无效的日期格式
    
    if end_date:
        try:
            # 结束日期包含整天，所以设置为当天的23:59:59
            end_datetime = datetime.strptime(end_date, '%Y-%m-%d')
            end_datetime = end_datetime.replace(hour=23, minute=59, second=59)
            query = query.filter(StudentGroup.submit_time <= end_datetime)
        except ValueError:
            pass  # 忽略无效的日期格式
    
    # 按小组编号升序，然后按提交时间倒序
    # 使用case语句将NULL值排在最后（设置为999999）
    query = query.order_by(
        case((StudentGroup.group_number.is_(None), 999999), else_=StudentGroup.group_number).asc(),
        StudentGroup.submit_time.desc()
    )
    
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    
    students = [student.to_dict() for student in pagination.items]
    
    return jsonify({
        'success': True,
        'data': students,
        'pagination': {
            'page': pagination.page,
            'pages': pagination.pages,
            'per_page': pagination.per_page,
            'total': pagination.total
        }
    })

@web_bp.route('/api/students/<submission_id>')
def api_student_detail(submission_id):
    """API: 获取学生详情"""
    group = StudentGroup.query.filter_by(submission_id=submission_id).first()
    
    if not group:
        return jsonify({
            'success': False,
            'message': '学生数据不存在'
        }), 404
    
    # 构建完整数据
    data = group.to_dict()
    
    # 添加成员信息
    data['members'] = [m.to_dict() for m in group.members]
    
    # 添加任务一数据
    if group.task1:
        data['task1'] = group.task1.to_dict()
        data['task1']['photos'] = [
            {'url': f'/static/photos/{Path(p.file_path).name}'}
            for p in group.photos if p.photo_type == 'task1'
        ]
    
    # 添加任务二数据
    if group.task2:
        data['task2'] = group.task2.to_dict()
        data['task2']['photos'] = [
            {'url': f'/static/photos/{Path(p.file_path).name}'}
            for p in group.photos if p.photo_type == 'task2'
        ]
    
    # 添加思考题数据
    data['thinking_questions'] = {}
    for thinking in group.thinking_questions:
        data['thinking_questions'][thinking.question_type] = thinking.to_dict()
        data['thinking_questions'][thinking.question_type]['photos'] = [
            {'url': f'/static/photos/{Path(p.file_path).name}'}
            for p in group.photos if p.photo_type == thinking.question_type
        ]
    
    # 添加茶助教问答记录
    chat_messages = ChatMessage.query.filter_by(group_id=group.id).order_by(ChatMessage.message_index).all()
    data['chat_messages'] = [msg.to_dict() for msg in chat_messages]
    
    # 统计学生提问数量
    user_questions = [msg for msg in chat_messages if msg.role == 'user']
    data['user_questions_count'] = len(user_questions)
    
    return jsonify({
        'success': True,
        'data': data
    })

@web_bp.route('/api/students/<submission_id>/delete', methods=['POST', 'DELETE'])
def delete_student(submission_id):
    """删除学生数据"""
    try:
        success, message = delete_student_data(submission_id)
        
        if success:
            return jsonify({
                'success': True,
                'message': message
            }), 200
        else:
            return jsonify({
                'success': False,
                'message': message
            }), 400
            
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'删除失败: {str(e)}'
        }), 500

@web_bp.route('/static/photos/<filename>')
def serve_photo(filename):
    """提供照片文件"""
    return send_from_directory(Config.UPLOAD_FOLDER, filename)

