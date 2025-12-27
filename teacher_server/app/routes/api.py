"""
API路由 - 接收学生数据
"""
from flask import Blueprint, request, jsonify
from app.services.data_service import save_all_data

api_bp = Blueprint('api', __name__)

@api_bp.route('/submit', methods=['POST'])
def submit_data():
    """接收学生提交的数据"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'message': '请求数据为空'
            }), 400
        
        # 保存数据
        success, message, submission_id = save_all_data(data)
        
        if success:
            return jsonify({
                'success': True,
                'message': message,
                'submissionId': submission_id
            }), 200
        else:
            return jsonify({
                'success': False,
                'message': message
            }), 400
            
    except Exception as e:
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

