# 茶文化课程APP - 教师端服务器

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 初始化数据库

```bash
python init_db.py
```

### 3. 启动服务器

```bash
python run.py
```

服务器将在 `http://0.0.0.0:8888` 启动

### 4. 访问Web界面

打开浏览器访问：`http://localhost:8888`

## API接口

### 提交学生数据

```
POST http://localhost:8888/api/submit
Content-Type: application/json

{
    "studentInfo": {
        "school": "测试学校",
        "grade": "高一",
        "classNumber": "1",
        "date": "2024-01-01",
        "memberCount": 3,
        "memberNames": ["张三", "李四", "王五"]
    },
    "task1": {
        "teaName": "龙井",
        "waterTemperature": "80",
        ...
    },
    ...
}
```

### 获取学生列表

```
GET http://localhost:8888/api/students?page=1&limit=20
```

### 获取学生详情

```
GET http://localhost:8888/api/students/{submission_id}
```

## 项目结构

```
teacher_server/
├── app/                    # 应用主目录
│   ├── __init__.py        # Flask应用初始化
│   ├── models.py          # 数据库模型
│   ├── routes/            # 路由
│   │   ├── api.py        # API路由
│   │   └── web.py        # Web路由
│   ├── services/          # 业务逻辑
│   │   ├── data_service.py
│   │   └── photo_service.py
│   ├── utils/            # 工具函数
│   │   └── validators.py
│   └── templates/         # HTML模板
│       ├── base.html
│       ├── index.html
│       └── student_detail.html
├── config.py              # 配置文件
├── run.py                 # 启动脚本
├── init_db.py            # 数据库初始化
└── requirements.txt      # Python依赖
```

## 配置说明

编辑 `config.py` 可以修改：
- 数据库连接
- 文件上传路径
- 分页大小
- 日志配置

## 注意事项

1. 首次运行前需要执行 `python init_db.py` 初始化数据库
2. 确保 `uploads/photos/` 目录有写入权限
3. 生产环境建议使用 PostgreSQL 数据库
4. 生产环境建议使用 Gunicorn + Nginx 部署

