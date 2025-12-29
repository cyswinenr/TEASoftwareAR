"""
API路由 - 接收学生数据
"""
from flask import Blueprint, request, jsonify
from app.services.data_service import save_all_data

api_bp = Blueprint('api', __name__)

@api_bp.route('/submit', methods=['POST'])
def submit_data():
    """
    接收学生提交的数据
    
    支持两种模式：
    1. 新建提交：不提供 submission_id，服务器创建新记录并返回 submission_id
    2. 更新提交：提供 submission_id，服务器验证并更新现有记录
    
    请求体示例（新建）：
    {
        "studentInfo": { ... },
        "task1": { ... }
    }
    
    请求体示例（更新）：
    {
        "submission_id": "学校_年级_班级_日期_哈希_时间戳",
        "studentInfo": { ... },
        "task2": { ... }
    }
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'message': '请求数据为空'
            }), 400
        
        # 检查是否提供了 submission_id（支持两种字段名）
        submission_id = data.get('submission_id') or data.get('submissionId')
        
        # 如果提供了 submission_id，从请求体中移除（避免影响后续处理）
        if submission_id:
            data.pop('submission_id', None)
            data.pop('submissionId', None)
        
        # 保存数据
        success, message, result_submission_id = save_all_data(data, submission_id)
        
        if success:
            return jsonify({
                'success': True,
                'message': message,
                'submissionId': result_submission_id
            }), 200
        else:
            # 返回详细的错误信息
            return jsonify({
                'success': False,
                'message': message
            }), 400
            
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'message': f'服务器错误: {str(e)}'
        }), 500

@api_bp.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'message': '服务器运行正常'
    }), 200

