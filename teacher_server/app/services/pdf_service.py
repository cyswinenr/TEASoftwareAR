"""
PDF导出服务
为每个小组生成精美的PDF报告，方便教师留档和展示
"""
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, PageBreak, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from datetime import datetime
from pathlib import Path
import os
import sys

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent.parent.parent))
from config import Config

class PDFExportService:
    """PDF导出服务类"""
    
    def __init__(self):
        # 注册中文字体
        self._register_fonts()
        
        # 创建样式
        self.styles = getSampleStyleSheet()
        self._create_custom_styles()
    
    def _register_fonts(self):
        """注册中文字体"""
        try:
            # 尝试使用系统字体
            # Windows系统字体路径
            font_paths = [
                'C:/Windows/Fonts/simhei.ttf',  # 黑体
                'C:/Windows/Fonts/simsun.ttc',  # 宋体
                'C:/Windows/Fonts/msyh.ttc',    # 微软雅黑
                '/System/Library/Fonts/PingFang.ttc',  # macOS
                '/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc',  # Linux
            ]
            
            font_registered = False
            for font_path in font_paths:
                if os.path.exists(font_path):
                    try:
                        pdfmetrics.registerFont(TTFont('Chinese', font_path))
                        font_registered = True
                        print(f"成功注册字体: {font_path}")
                        break
                    except:
                        continue
            
            if not font_registered:
                print("警告: 未找到中文字体，将使用默认字体（可能无法显示中文）")
                
        except Exception as e:
            print(f"注册字体失败: {e}")
    
    def _create_custom_styles(self):
        """创建自定义样式"""
        # 标题样式
        self.title_style = ParagraphStyle(
            'CustomTitle',
            parent=self.styles['Heading1'],
            fontName='Chinese',
            fontSize=24,
            textColor=colors.HexColor('#2E7D32'),
            alignment=TA_CENTER,
            spaceAfter=20
        )
        
        # 副标题样式
        self.heading2_style = ParagraphStyle(
            'CustomHeading2',
            parent=self.styles['Heading2'],
            fontName='Chinese',
            fontSize=16,
            textColor=colors.HexColor('#2E7D32'),
            spaceAfter=12,
            spaceBefore=12
        )
        
        # 小标题样式
        self.heading3_style = ParagraphStyle(
            'CustomHeading3',
            parent=self.styles['Heading3'],
            fontName='Chinese',
            fontSize=14,
            textColor=colors.HexColor('#424242'),
            spaceAfter=10,
            spaceBefore=10
        )
        
        # 正文样式
        self.body_style = ParagraphStyle(
            'CustomBody',
            parent=self.styles['BodyText'],
            fontName='Chinese',
            fontSize=11,
            leading=18,
            spaceAfter=10
        )
        
        # 小字体样式
        self.small_style = ParagraphStyle(
            'CustomSmall',
            parent=self.styles['BodyText'],
            fontName='Chinese',
            fontSize=9,
            textColor=colors.HexColor('#666666')
        )
    
    def generate_group_pdf(self, group, output_path=None):
        """
        为单个小组生成PDF报告
        
        Args:
            group: StudentGroup对象
            output_path: 输出路径，如果为None则自动生成
        
        Returns:
            生成的PDF文件路径
        """
        if output_path is None:
            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
            filename = f"茶文化课程报告_{group.school}_{group.grade}{group.class_number}班_小组{group.group_number or '未设置'}_{timestamp}.pdf"
            output_path = filename
        
        # 创建PDF文档
        doc = SimpleDocTemplate(
            output_path,
            pagesize=A4,
            rightMargin=2*cm,
            leftMargin=2*cm,
            topMargin=2*cm,
            bottomMargin=2*cm
        )
        
        # 构建内容
        story = []
        
        # 添加标题
        story.append(Paragraph("茶文化课程学习报告", self.title_style))
        story.append(Spacer(1, 0.5*cm))
        
        # 添加基本信息
        self._add_basic_info(story, group)
        story.append(Spacer(1, 0.5*cm))
        
        # 添加任务一
        if group.task1:
            self._add_task1(story, group)
            story.append(Spacer(1, 0.5*cm))
        
        # 添加任务二
        if group.task2:
            self._add_task2(story, group)
            story.append(Spacer(1, 0.5*cm))
        
        # 添加思考题
        if group.thinking_questions:
            self._add_thinking_questions(story, group)
            story.append(Spacer(1, 0.5*cm))
        
        # 添加茶助教问答记录
        if group.chat_messages:
            self._add_chat_messages(story, group)
        
        # 添加页脚信息
        story.append(Spacer(1, 1*cm))
        footer_text = f"生成时间：{datetime.now().strftime('%Y年%m月%d日 %H:%M:%S')}"
        story.append(Paragraph(footer_text, self.small_style))
        
        # 生成PDF
        doc.build(story)
        
        return output_path
    
    def _add_basic_info(self, story, group):
        """添加学生基本信息"""
        story.append(Paragraph("一、学生基本信息", self.heading2_style))
        
        # 创建信息表格
        data = [
            ['小组编号', str(group.group_number) if group.group_number else '未设置', '学校', group.school],
            ['年级', group.grade, '班级', f'{group.class_number}班'],
            ['活动日期', group.activity_date.strftime('%Y年%m月%d日') if group.activity_date else '', 
             '成员人数', f'{group.member_count}人'],
            ['提交时间', group.submit_time.strftime('%Y年%m月%d日 %H:%M:%S') if group.submit_time else '', '', '']
        ]
        
        table = Table(data, colWidths=[3*cm, 4*cm, 3*cm, 4*cm])
        table.setStyle(TableStyle([
            ('FONTNAME', (0, 0), (-1, -1), 'Chinese'),
            ('FONTSIZE', (0, 0), (-1, -1), 10),
            ('BACKGROUND', (0, 0), (0, -1), colors.HexColor('#E8F5E9')),
            ('BACKGROUND', (2, 0), (2, -1), colors.HexColor('#E8F5E9')),
            ('TEXTCOLOR', (0, 0), (0, -1), colors.HexColor('#2E7D32')),
            ('TEXTCOLOR', (2, 0), (2, -1), colors.HexColor('#2E7D32')),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('ROWBACKGROUNDS', (0, 0), (-1, -1), [colors.white, colors.HexColor('#F5F5F5')]),
            ('PADDING', (0, 0), (-1, -1), 8),
        ]))
        story.append(table)
        
        # 小组成员
        story.append(Spacer(1, 0.3*cm))
        members_text = "小组成员：" + "、".join([m.member_name for m in group.members])
        story.append(Paragraph(members_text, self.body_style))
    
    def _add_task1(self, story, group):
        """添加任务一数据"""
        story.append(Paragraph("二、任务一：泡茶体验、品茶时刻", self.heading2_style))
        
        task1 = group.task1
        
        # 茶品信息
        if task1.tea_name or task1.teacher_tea_name or task1.tea_category:
            story.append(Paragraph("（一）茶品信息", self.heading3_style))
            
            # 创建单元格样式（支持自动换行）
            info_cell_style = ParagraphStyle(
                'InfoCellStyle',
                parent=self.body_style,
                fontName='Chinese',
                fontSize=10,
                leading=14,
                wordWrap='CJK'
            )
            
            info_data = [
                ['茶品名', Paragraph(task1.tea_name or '未填写', info_cell_style), 
                 '老师茶品名', Paragraph(task1.teacher_tea_name or '未填写', info_cell_style)],
                ['茶类', Paragraph(task1.tea_category or '未填写', info_cell_style), 
                 '水温', Paragraph(f"{task1.water_temperature}°C" if task1.water_temperature else '未填写', info_cell_style)],
                ['冲泡时长', Paragraph(task1.brewing_duration or '未填写', info_cell_style), '', '']
            ]
            
            table = Table(info_data, colWidths=[3*cm, 4*cm, 3*cm, 4*cm])
            table.setStyle(self._get_table_style())
            story.append(table)
            story.append(Spacer(1, 0.3*cm))
        
        # 感官记录
        story.append(Paragraph("（二）同款茶不同形态的感官记录", self.heading3_style))
        records = task1.get_sensory_records()
        
        # 创建表格单元格样式（支持自动换行）
        cell_style = ParagraphStyle(
            'CellStyle',
            parent=self.body_style,
            fontName='Chinese',
            fontSize=9,
            leading=12,
            alignment=TA_CENTER,
            wordWrap='CJK'
        )
        
        # 将所有单元格文本包裹在Paragraph中
        sensory_data = [
            ['', '色泽（观看）', '香气（轻嗅）', '形状（观看）', '滋味（品尝）'],
            ['干茶',
             Paragraph(records.get('dryTea', {}).get('color', '') or '未填写', cell_style),
             Paragraph(records.get('dryTea', {}).get('aroma', '') or '未填写', cell_style),
             Paragraph(records.get('dryTea', {}).get('shape', '') or '未填写', cell_style),
             Paragraph(records.get('dryTea', {}).get('taste', '') or '未填写', cell_style)],
            ['茶汤',
             Paragraph(records.get('teaLiquor', {}).get('color', '') or '未填写', cell_style),
             Paragraph(records.get('teaLiquor', {}).get('aroma', '') or '未填写', cell_style),
             Paragraph(records.get('teaLiquor', {}).get('shape', '') or '未填写', cell_style),
             Paragraph(records.get('teaLiquor', {}).get('taste', '') or '未填写', cell_style)],
            ['叶底',
             Paragraph(records.get('spentLeaves', {}).get('color', '') or '未填写', cell_style),
             Paragraph(records.get('spentLeaves', {}).get('aroma', '') or '未填写', cell_style),
             Paragraph(records.get('spentLeaves', {}).get('shape', '') or '未填写', cell_style),
             Paragraph(records.get('spentLeaves', {}).get('taste', '') or '未填写', cell_style)]
        ]
        
        table = Table(sensory_data, colWidths=[2.5*cm, 3*cm, 3*cm, 3*cm, 3*cm])
        table.setStyle(TableStyle([
            ('FONTNAME', (0, 0), (-1, -1), 'Chinese'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#4CAF50')),
            ('BACKGROUND', (0, 1), (0, -1), colors.HexColor('#E8F5E9')),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
            ('TEXTCOLOR', (0, 1), (0, -1), colors.HexColor('#2E7D32')),
            ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('PADDING', (0, 0), (-1, -1), 6),
        ]))
        story.append(table)
        
        # 思考题
        if task1.reflection_answer:
            story.append(Spacer(1, 0.3*cm))
            story.append(Paragraph("（三）品评其他组同类的茶滋味，有何异同？为什么？", self.heading3_style))
            story.append(Paragraph(task1.reflection_answer, self.body_style))
    
    def _add_task2(self, story, group):
        """添加任务二数据"""
        story.append(Paragraph("三、任务二：泡出你心中的那杯茶", self.heading2_style))
        
        task2 = group.task2
        
        # 茶品信息
        story.append(Paragraph("（一）第二次冲泡的关键因素控制及茶汤的特点记录", self.heading3_style))
        
        # 创建单元格样式（支持自动换行）
        task2_cell_style = ParagraphStyle(
            'Task2CellStyle',
            parent=self.body_style,
            fontName='Chinese',
            fontSize=10,
            leading=14,
            wordWrap='CJK'
        )
        
        data = [
            ['茶品名', Paragraph(task2.tea_name or '未填写', task2_cell_style), 
             '冲泡的水温', Paragraph(f"{task2.water_temperature}°C" if task2.water_temperature else '未填写', task2_cell_style)],
            ['出汤的时长', Paragraph(task2.steeping_duration or '未填写', task2_cell_style), '', ''],
            ['茶汤的色泽', Paragraph(task2.tea_color or '未填写', task2_cell_style), 
             '茶汤的香气', Paragraph(task2.tea_aroma or '未填写', task2_cell_style)],
            ['茶汤的滋味', Paragraph(task2.tea_taste or '未填写', task2_cell_style), '', '']
        ]
        
        table = Table(data, colWidths=[3*cm, 4*cm, 3*cm, 4*cm])
        table.setStyle(self._get_table_style())
        story.append(table)
        
        # 是否符合预期
        story.append(Spacer(1, 0.3*cm))
        expectation_text = "符合预期：" + ("是" if task2.meets_expectation else "否") + " | " + \
                          "不符合预期：" + ("是" if task2.not_meets_expectation else "否")
        story.append(Paragraph(expectation_text, self.body_style))
        
        # 思考题
        if task2.reflection_answer:
            story.append(Spacer(1, 0.3*cm))
            story.append(Paragraph("（二）现冲泡的茶滋味是否符合心中的？你觉得符合预期/不符合预期的关键点在哪里？", self.heading3_style))
            story.append(Paragraph(task2.reflection_answer, self.body_style))
    
    def _add_thinking_questions(self, story, group):
        """添加思考题"""
        thinking_dict = {t.question_type: t for t in group.thinking_questions}
        
        # 思考题一
        if 'thinking1' in thinking_dict:
            story.append(Paragraph("四、思考题一", self.heading2_style))
            story.append(Paragraph("通过今天的课程，你们对茶文化有了哪些新的认识？你们喜欢课程的哪些环节？还有没有其他想要了解的茶文化内容？", self.body_style))
            if thinking_dict['thinking1'].answer:
                story.append(Paragraph(thinking_dict['thinking1'].answer, self.body_style))
            story.append(Spacer(1, 0.3*cm))
        
        # 思考题二
        if 'thinking2' in thinking_dict:
            story.append(Paragraph("五、思考题二", self.heading2_style))
            story.append(Paragraph("通过亲身体验，感受茶文化，你觉得茶为什么可以成为'中国文化名片'？", self.body_style))
            if thinking_dict['thinking2'].answer:
                story.append(Paragraph(thinking_dict['thinking2'].answer, self.body_style))
            story.append(Spacer(1, 0.3*cm))
        
        # 创意题
        if 'creative' in thinking_dict:
            story.append(Paragraph("六、创意题", self.heading2_style))
            if thinking_dict['creative'].answer:
                story.append(Paragraph(thinking_dict['creative'].answer, self.body_style))
            story.append(Spacer(1, 0.3*cm))
    
    def _add_chat_messages(self, story, group):
        """添加茶助教问答记录"""
        story.append(Paragraph("七、茶助教问答记录", self.heading2_style))
        
        chat_messages = sorted(group.chat_messages, key=lambda x: x.message_index)
        
        # 统计信息
        user_count = len([m for m in chat_messages if m.role == 'user'])
        assistant_count = len([m for m in chat_messages if m.role == 'assistant'])
        
        stats_text = f"学生提问：{user_count}次 | AI回答：{assistant_count}次 | 总对话：{len(chat_messages)}轮"
        story.append(Paragraph(stats_text, self.body_style))
        story.append(Spacer(1, 0.3*cm))
        
        # 显示对话（只显示前10轮，避免PDF过长）
        display_count = min(10, len(chat_messages))
        
        for i, msg in enumerate(chat_messages[:display_count]):
            role_name = "👤 学生" if msg.role == 'user' else "🤖 茶助教"
            bg_color = colors.HexColor('#E8F5E9') if msg.role == 'user' else colors.HexColor('#F3E5F5')
            
            # 创建对话框样式（支持自动换行）
            chat_style = ParagraphStyle(
                'ChatStyle',
                parent=self.body_style,
                fontName='Chinese',
                fontSize=10,
                leading=16,
                wordWrap='CJK'  # 支持中文换行
            )
            
            # 使用Paragraph对象处理文本，确保长文本能自动换行
            content_paragraph = Paragraph(f"<b>{role_name}：</b>{msg.content}", chat_style)
            
            data = [[content_paragraph]]
            table = Table(data, colWidths=[14*cm])
            table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, -1), bg_color),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('VALIGN', (0, 0), (-1, -1), 'TOP'),
                ('PADDING', (0, 0), (-1, -1), 10),
                ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ]))
            story.append(table)
            story.append(Spacer(1, 0.2*cm))
        
        if len(chat_messages) > display_count:
            remaining = len(chat_messages) - display_count
            story.append(Paragraph(f"（还有 {remaining} 条对话未显示，请查看完整数据）", self.small_style))
    
    def _get_table_style(self):
        """获取通用表格样式"""
        return TableStyle([
            ('FONTNAME', (0, 0), (-1, -1), 'Chinese'),
            ('FONTSIZE', (0, 0), (-1, -1), 10),
            ('BACKGROUND', (0, 0), (0, -1), colors.HexColor('#E8F5E9')),
            ('BACKGROUND', (2, 0), (2, -1), colors.HexColor('#E8F5E9')),
            ('TEXTCOLOR', (0, 0), (0, -1), colors.HexColor('#2E7D32')),
            ('TEXTCOLOR', (2, 0), (2, -1), colors.HexColor('#2E7D32')),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('PADDING', (0, 0), (-1, -1), 8),
        ])

