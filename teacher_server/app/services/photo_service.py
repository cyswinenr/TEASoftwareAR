"""
照片处理服务
"""
import os
import base64
from pathlib import Path
from datetime import datetime
from PIL import Image
import io
from app import db
from app.models import Photo
from pathlib import Path
import sys
# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent.parent))
from config import Config

def save_photo_from_base64(base64_str, group_id, photo_type, photo_index, submission_id):
    """
    从Base64字符串保存照片
    
    Args:
        base64_str: Base64编码的照片数据
        group_id: 学生组ID
        photo_type: 照片类型（task1/task2/thinking1/thinking2/creative/info）
        photo_index: 照片序号
        submission_id: 提交ID（用于生成文件名）
    
    Returns:
        Photo对象或None
    """
    try:
        # 解码Base64
        image_data = base64.b64decode(base64_str)
        
        # 验证图片格式
        try:
            img = Image.open(io.BytesIO(image_data))
            img.verify()
        except:
            return None
        
        # 重新打开图片（verify后需要重新打开）
        img = Image.open(io.BytesIO(image_data))
        
        # 生成文件名
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        file_name = f"{submission_id}_{photo_type}_{photo_index}_{timestamp}.jpg"
        
        # 确保目录存在
        upload_folder = Path(Config.UPLOAD_FOLDER)
        upload_folder.mkdir(parents=True, exist_ok=True)
        
        # 保存文件
        file_path = upload_folder / file_name
        img.save(file_path, 'JPEG', quality=85)
        
        # 获取文件大小
        file_size = file_path.stat().st_size
        
        # 创建Photo记录
        photo = Photo(
            group_id=group_id,
            photo_type=photo_type,
            photo_index=photo_index,
            file_path=str(file_path.relative_to(Path(Config.UPLOAD_FOLDER).parent)),
            file_name=file_name,
            file_size=file_size
        )
        
        db.session.add(photo)
        return photo
        
    except Exception as e:
        print(f"保存照片失败: {e}")
        return None

def get_photo_url(photo):
    """获取照片URL"""
    if not photo:
        return None
    # 返回相对路径，前端可以通过/static/photos/访问
    return f"/static/photos/{Path(photo.file_path).name}"

def delete_photo(photo):
    """删除照片文件和记录"""
    if not photo:
        return False
    
    try:
        # 删除文件
        file_path = Path(Config.UPLOAD_FOLDER) / Path(photo.file_path).name
        if file_path.exists():
            file_path.unlink()
        
        # 删除数据库记录
        db.session.delete(photo)
        return True
    except Exception as e:
        print(f"删除照片失败: {e}")
        return False

