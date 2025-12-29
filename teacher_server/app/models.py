"""
数据库模型定义
"""
from app import db
from datetime import datetime
import json

class StudentGroup(db.Model):
    """学生组表"""
    __tablename__ = 'student_groups'
    
    id = db.Column(db.Integer, primary_key=True)
    submission_id = db.Column(db.String(64), unique=True, nullable=False, index=True)
    school = db.Column(db.String(100), nullable=False, index=True)
    grade = db.Column(db.String(10), nullable=False, index=True)
    class_number = db.Column(db.String(10), nullable=False, index=True)
    activity_date = db.Column(db.Date, nullable=False, index=True)
    member_count = db.Column(db.Integer, nullable=False)
    submit_time = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    version = db.Column(db.String(10), default='1.0')
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # 关系
    members = db.relationship('GroupMember', backref='group', lazy=True, cascade='all, delete-orphan')
    task1 = db.relationship('Task1Data', backref='group', uselist=False, cascade='all, delete-orphan')
    task2 = db.relationship('Task2Data', backref='group', uselist=False, cascade='all, delete-orphan')
    thinking_questions = db.relationship('ThinkingQuestion', backref='group', lazy=True, cascade='all, delete-orphan')
    photos = db.relationship('Photo', backref='group', lazy=True, cascade='all, delete-orphan')
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'submission_id': self.submission_id,
            'school': self.school,
            'grade': self.grade,
            'class_number': self.class_number,
            'activity_date': self.activity_date.isoformat() if self.activity_date else None,
            'member_count': self.member_count,
            'submit_time': self.submit_time.isoformat() if self.submit_time else None,
            'version': self.version
        }
    
    def get_task1_char_count(self):
        """计算任务一的总字符数"""
        if not self.task1:
            return 0
        
        count = 0
        if self.task1.tea_name:
            count += len(self.task1.tea_name)
        if self.task1.teacher_tea_name:
            count += len(self.task1.teacher_tea_name)
        if self.task1.reflection_answer:
            count += len(self.task1.reflection_answer)
        
        # 统计感官记录
        records = self.task1.get_sensory_records()
        for key, value in records.items():
            if value:
                for sub_key, sub_value in value.items():
                    if sub_value:
                        count += len(sub_value)
        
        return count
    
    def get_task2_char_count(self):
        """计算任务二的总字符数"""
        if not self.task2:
            return 0
        
        count = 0
        if self.task2.tea_name:
            count += len(self.task2.tea_name)
        if self.task2.tea_color:
            count += len(self.task2.tea_color)
        if self.task2.tea_aroma:
            count += len(self.task2.tea_aroma)
        if self.task2.tea_taste:
            count += len(self.task2.tea_taste)
        if self.task2.reflection_answer:
            count += len(self.task2.reflection_answer)
        
        return count
    
    def get_thinking_char_count(self, question_type):
        """计算思考题的字符数"""
        thinking = next((t for t in self.thinking_questions if t.question_type == question_type), None)
        if thinking and thinking.answer:
            return len(thinking.answer.strip())
        return 0


class GroupMember(db.Model):
    """小组成员表"""
    __tablename__ = 'group_members'
    
    id = db.Column(db.Integer, primary_key=True)
    group_id = db.Column(db.Integer, db.ForeignKey('student_groups.id', ondelete='CASCADE'), nullable=False)
    member_index = db.Column(db.Integer, nullable=False)
    member_name = db.Column(db.String(50), nullable=False)
    
    __table_args__ = (db.UniqueConstraint('group_id', 'member_index', name='uq_group_member'),)
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'member_index': self.member_index,
            'member_name': self.member_name
        }


class Task1Data(db.Model):
    """任务一数据表"""
    __tablename__ = 'task1_data'
    
    id = db.Column(db.Integer, primary_key=True)
    group_id = db.Column(db.Integer, db.ForeignKey('student_groups.id', ondelete='CASCADE'), nullable=False, unique=True)
    
    # 茶品信息
    tea_name = db.Column(db.String(100))
    teacher_tea_name = db.Column(db.String(100))
    tea_category = db.Column(db.String(50))
    water_temperature = db.Column(db.String(20))
    brewing_duration = db.Column(db.String(50))
    
    # 感官记录（JSON格式）
    sensory_records = db.Column(db.Text)  # JSON格式存储
    
    # 思考题
    reflection_answer = db.Column(db.Text)
    
    # 元数据
    submit_time = db.Column(db.DateTime, default=datetime.utcnow)
    version = db.Column(db.String(10), default='1.0')
    
    def get_sensory_records(self):
        """获取感官记录（解析JSON）"""
        if self.sensory_records:
            try:
                return json.loads(self.sensory_records)
            except:
                return {}
        return {}
    
    def set_sensory_records(self, data):
        """设置感官记录（序列化为JSON）"""
        self.sensory_records = json.dumps(data, ensure_ascii=False)
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'tea_name': self.tea_name,
            'teacher_tea_name': self.teacher_tea_name,
            'tea_category': self.tea_category,
            'water_temperature': self.water_temperature,
            'brewing_duration': self.brewing_duration,
            'sensory_records': self.get_sensory_records(),
            'reflection_answer': self.reflection_answer,
            'submit_time': self.submit_time.isoformat() if self.submit_time else None
        }


class Task2Data(db.Model):
    """任务二数据表"""
    __tablename__ = 'task2_data'
    
    id = db.Column(db.Integer, primary_key=True)
    group_id = db.Column(db.Integer, db.ForeignKey('student_groups.id', ondelete='CASCADE'), nullable=False, unique=True)
    
    # 茶品信息
    tea_name = db.Column(db.String(100))
    water_temperature = db.Column(db.String(20))
    steeping_duration = db.Column(db.String(50))
    
    # 茶汤特点
    tea_color = db.Column(db.String(100))
    tea_aroma = db.Column(db.String(100))
    tea_taste = db.Column(db.String(100))
    
    # 是否符合预期
    meets_expectation = db.Column(db.Boolean, default=False)
    not_meets_expectation = db.Column(db.Boolean, default=False)
    
    # 思考题
    reflection_answer = db.Column(db.Text)
    
    # 元数据
    submit_time = db.Column(db.DateTime, default=datetime.utcnow)
    version = db.Column(db.String(10), default='1.0')
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'tea_name': self.tea_name,
            'water_temperature': self.water_temperature,
            'steeping_duration': self.steeping_duration,
            'tea_color': self.tea_color,
            'tea_aroma': self.tea_aroma,
            'tea_taste': self.tea_taste,
            'meets_expectation': self.meets_expectation,
            'not_meets_expectation': self.not_meets_expectation,
            'reflection_answer': self.reflection_answer,
            'submit_time': self.submit_time.isoformat() if self.submit_time else None
        }


class ThinkingQuestion(db.Model):
    """思考题数据表"""
    __tablename__ = 'thinking_questions'
    
    id = db.Column(db.Integer, primary_key=True)
    group_id = db.Column(db.Integer, db.ForeignKey('student_groups.id', ondelete='CASCADE'), nullable=False)
    question_type = db.Column(db.String(20), nullable=False, index=True)  # thinking1/thinking2/creative
    answer = db.Column(db.Text)
    submit_time = db.Column(db.DateTime, default=datetime.utcnow)
    version = db.Column(db.String(10), default='1.0')
    
    __table_args__ = (db.UniqueConstraint('group_id', 'question_type', name='uq_group_question'),)
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'question_type': self.question_type,
            'answer': self.answer,
            'submit_time': self.submit_time.isoformat() if self.submit_time else None
        }


class Photo(db.Model):
    """照片表"""
    __tablename__ = 'photos'
    
    id = db.Column(db.Integer, primary_key=True)
    group_id = db.Column(db.Integer, db.ForeignKey('student_groups.id', ondelete='CASCADE'), nullable=False, index=True)
    photo_type = db.Column(db.String(20), nullable=False, index=True)  # task1/task2/thinking1/thinking2/creative/info
    photo_index = db.Column(db.Integer, nullable=False)
    file_path = db.Column(db.String(500), nullable=False)
    file_name = db.Column(db.String(200), nullable=False)
    file_size = db.Column(db.Integer)
    upload_time = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        """转换为字典"""
        return {
            'id': self.id,
            'photo_type': self.photo_type,
            'photo_index': self.photo_index,
            'file_path': self.file_path,
            'file_name': self.file_name,
            'file_size': self.file_size,
            'upload_time': self.upload_time.isoformat() if self.upload_time else None
        }

