package Teacourse.apk.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import Teacourse.apk.utils.ChatHistoryManager
import Teacourse.apk.utils.ChatMessage

@Composable
fun SummaryScreen(
    onBackClick: () -> Unit,
    onNavigateToTask1: () -> Unit,
    onNavigateToTask2: () -> Unit,
    onNavigateToThinking1: () -> Unit,
    onNavigateToThinking2: () -> Unit
) {
    val context = LocalContext.current
    
    // 加载所有数据
    val task1Prefs = remember { context.getSharedPreferences("Task1Data", Context.MODE_PRIVATE) }
    val task2Prefs = remember { context.getSharedPreferences("Task2Data", Context.MODE_PRIVATE) }
    val thinking1Prefs = remember { context.getSharedPreferences("Thinking1Data", Context.MODE_PRIVATE) }
    val thinking2Prefs = remember { context.getSharedPreferences("Thinking2Data", Context.MODE_PRIVATE) }
    
    // 任务一数据
    val task1Data = remember {
        mapOf(
            "dryTeaColor" to (task1Prefs.getString("dryTeaColor", "") ?: ""),
            "dryTeaAroma" to (task1Prefs.getString("dryTeaAroma", "") ?: ""),
            "dryTeaShape" to (task1Prefs.getString("dryTeaShape", "") ?: ""),
            "dryTeaTaste" to (task1Prefs.getString("dryTeaTaste", "") ?: ""),
            "teaLiquorColor" to (task1Prefs.getString("teaLiquorColor", "") ?: ""),
            "teaLiquorAroma" to (task1Prefs.getString("teaLiquorAroma", "") ?: ""),
            "teaLiquorShape" to (task1Prefs.getString("teaLiquorShape", "") ?: ""),
            "teaLiquorTaste" to (task1Prefs.getString("teaLiquorTaste", "") ?: ""),
            "spentLeavesColor" to (task1Prefs.getString("spentLeavesColor", "") ?: ""),
            "spentLeavesAroma" to (task1Prefs.getString("spentLeavesAroma", "") ?: ""),
            "spentLeavesShape" to (task1Prefs.getString("spentLeavesShape", "") ?: ""),
            "spentLeavesTaste" to (task1Prefs.getString("spentLeavesTaste", "") ?: ""),
            "waterTemperature" to (task1Prefs.getString("waterTemperature", "") ?: ""),
            "brewingDuration" to (task1Prefs.getString("brewingDuration", "") ?: ""),
            "teaCategory" to (task1Prefs.getString("teaCategory", "") ?: ""),
            "teaName" to (task1Prefs.getString("teaName", "") ?: ""),
            "teacherTeaName" to (task1Prefs.getString("teacherTeaName", "") ?: ""),
            "reflectionAnswer" to (task1Prefs.getString("reflectionAnswer", "") ?: ""),
            "photoPaths" to (task1Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).toList()
        )
    }
    
    // 任务二数据
    val task2Data = remember {
        mapOf(
            "teaName" to (task2Prefs.getString("teaName", "") ?: ""),
            "waterTemperature" to (task2Prefs.getString("waterTemperature", "") ?: ""),
            "steepingDuration" to (task2Prefs.getString("steepingDuration", "") ?: ""),
            "teaColor" to (task2Prefs.getString("teaColor", "") ?: ""),
            "teaAroma" to (task2Prefs.getString("teaAroma", "") ?: ""),
            "teaTaste" to (task2Prefs.getString("teaTaste", "") ?: ""),
            "meetsExpectation" to task2Prefs.getBoolean("meetsExpectation", false),
            "notMeetsExpectation" to task2Prefs.getBoolean("notMeetsExpectation", false),
            "reflectionAnswer" to (task2Prefs.getString("reflectionAnswer", "") ?: ""),
            "photoPaths" to (task2Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).toList()
        )
    }
    
    // 思考题一数据
    val thinking1Data = remember {
        mapOf(
            "answer" to (thinking1Prefs.getString("answer", "") ?: ""),
            "photoPaths" to (thinking1Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).toList()
        )
    }
    
    // 思考题二数据
    val thinking2Data = remember {
        mapOf(
            "answer" to (thinking2Prefs.getString("answer", "") ?: ""),
            "photoPaths" to (thinking2Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).toList()
        )
    }

    // 茶助教记录（使用永久历史，不会被删除）
    val chatHistoryManager = remember { ChatHistoryManager(context) }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var studentQuestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // 加载数据
    LaunchedEffect(Unit) {
        chatMessages = chatHistoryManager.loadChatMessagesPermanent()
        studentQuestions = chatHistoryManager.getUserQuestions()
    }

    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(30.dp)
        ) {
            // 标题和返回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "内容汇总",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("返回", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 任务一汇总
            SummarySection(
                title = "1. 任务一：泡茶体验、品茶时刻",
                color = Color(0xFF4CAF50),
                onEditClick = onNavigateToTask1
            ) {
                // 茶品信息
                val teaName1 = task1Data["teaName"] as? String ?: ""
                val teacherTeaName1 = task1Data["teacherTeaName"] as? String ?: ""
                val teaCategory1 = task1Data["teaCategory"] as? String ?: ""
                val waterTemp1 = task1Data["waterTemperature"] as? String ?: ""
                val brewingDur1 = task1Data["brewingDuration"] as? String ?: ""
                
                if (teaName1.isNotEmpty() || teacherTeaName1.isNotEmpty() || teaCategory1.isNotEmpty() || 
                    waterTemp1.isNotEmpty() || brewingDur1.isNotEmpty()) {
                    SummarySubsection("茶品信息") {
                        if (teaName1.isNotEmpty()) {
                            SummaryItem("茶品名", teaName1)
                        }
                        if (teacherTeaName1.isNotEmpty()) {
                            SummaryItem("老师茶品名", teacherTeaName1)
                        }
                        if (teaCategory1.isNotEmpty()) {
                            SummaryItem("茶类", teaCategory1)
                        }
                        if (waterTemp1.isNotEmpty()) {
                            SummaryItem("冲泡的水温", "${waterTemp1}°C")
                        }
                        if (brewingDur1.isNotEmpty()) {
                            SummaryItem("冲泡的时长", brewingDur1)
                        }
                    }
                }
                
                // 感官记录表格
                SummarySubsection("(1)同款茶不同形态的感官记录") {
                    SummaryTable(
                        headers = listOf("", "色泽(观看)", "香气(轻嗅)", "形状(观看)", "滋味(品尝)"),
                        rows = listOf(
                            listOf("干茶", 
                                task1Data["dryTeaColor"] as? String ?: "",
                                task1Data["dryTeaAroma"] as? String ?: "",
                                task1Data["dryTeaShape"] as? String ?: "",
                                task1Data["dryTeaTaste"] as? String ?: ""
                            ),
                            listOf("茶汤", 
                                task1Data["teaLiquorColor"] as? String ?: "",
                                task1Data["teaLiquorAroma"] as? String ?: "",
                                task1Data["teaLiquorShape"] as? String ?: "",
                                task1Data["teaLiquorTaste"] as? String ?: ""
                            ),
                            listOf("叶底", 
                                task1Data["spentLeavesColor"] as? String ?: "",
                                task1Data["spentLeavesAroma"] as? String ?: "",
                                task1Data["spentLeavesShape"] as? String ?: "",
                                task1Data["spentLeavesTaste"] as? String ?: ""
                            )
                        )
                    )
                }
                
                // 思考题
                val reflection1 = task1Data["reflectionAnswer"] as? String ?: ""
                if (reflection1.isNotEmpty()) {
                    SummarySubsection("(2)品评其他组同类的茶滋味,有何异同?为什么?") {
                        SummaryTextContent(reflection1)
                    }
                }
                
                // 照片
                val task1Photos = (task1Data["photoPaths"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (task1Photos.isNotEmpty()) {
                    SummarySubsection("照片") {
                        PhotoGrid(photoPaths = task1Photos)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 任务二汇总
            SummarySection(
                title = "2. 任务二：泡出你心中的那杯茶",
                color = Color(0xFF4CAF50),
                onEditClick = onNavigateToTask2
            ) {
                // 茶品信息
                val teaName2 = task2Data["teaName"] as? String ?: ""
                val waterTemp2 = task2Data["waterTemperature"] as? String ?: ""
                val steepingDur2 = task2Data["steepingDuration"] as? String ?: ""
                
                if (teaName2.isNotEmpty() || waterTemp2.isNotEmpty() || steepingDur2.isNotEmpty()) {
                    SummarySubsection("茶品信息") {
                        if (teaName2.isNotEmpty()) {
                            SummaryItem("茶品名", teaName2)
                        }
                        if (waterTemp2.isNotEmpty()) {
                            SummaryItem("冲泡的水温", "${waterTemp2}°C")
                        }
                        if (steepingDur2.isNotEmpty()) {
                            SummaryItem("出汤的时长", steepingDur2)
                        }
                    }
                }
                
                // 茶汤特点记录
                val teaColor2 = task2Data["teaColor"] as? String ?: ""
                val teaAroma2 = task2Data["teaAroma"] as? String ?: ""
                val teaTaste2 = task2Data["teaTaste"] as? String ?: ""
                
                if (teaColor2.isNotEmpty() || teaAroma2.isNotEmpty() || teaTaste2.isNotEmpty()) {
                    SummarySubsection("第二次冲泡的关键因素控制及茶汤的特点记录") {
                        SummaryTable(
                            headers = listOf("冲泡的水温", "出汤的时长", "茶汤的色泽(观看)", "茶汤的香气(轻嗅)", "茶汤的滋味(品尝)"),
                            rows = listOf(
                                listOf(
                                    if (waterTemp2.isNotEmpty()) "$waterTemp2°C" else "",
                                    steepingDur2,
                                    teaColor2,
                                    teaAroma2,
                                    teaTaste2
                                )
                            )
                        )
                    }
                }
                
                // 思考题
                val meets = task2Data["meetsExpectation"] as? Boolean ?: false
                val notMeets = task2Data["notMeetsExpectation"] as? Boolean ?: false
                val reflection2 = task2Data["reflectionAnswer"] as? String ?: ""
                
                if (meets || notMeets || reflection2.isNotEmpty()) {
                    SummarySubsection("(2)现冲泡的茶滋味是否符合心中的? 你觉得符合预期口/不符合预期口的 关键点在哪里?") {
                        if (meets || notMeets) {
                            Row(
                                modifier = Modifier.padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                if (meets) {
                                    Text("✓ 符合预期", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                }
                                if (notMeets) {
                                    Text("✓ 不符合预期", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (reflection2.isNotEmpty()) {
                            SummaryTextContent(reflection2)
                        }
                    }
                }
                
                // 照片
                val task2Photos = (task2Data["photoPaths"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (task2Photos.isNotEmpty()) {
                    SummarySubsection("照片") {
                        PhotoGrid(photoPaths = task2Photos)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 思考题一汇总
            SummarySection(
                title = "3. 思考题一",
                color = Color(0xFFFF9800),
                onEditClick = onNavigateToThinking1
            ) {
                SummarySubsection("题目") {
                    Text(
                        text = "通过今天的课程,你们对茶文化有了哪些新的认识?\n你们喜欢课程的哪些环节?\n还有没有其他想要了解的茶文化内容?",
                        fontSize = 16.sp,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                val answer1 = thinking1Data["answer"] as? String ?: ""
                if (answer1.isNotEmpty()) {
                    SummarySubsection("答案") {
                        SummaryTextContent(answer1)
                    }
                }
                
                // 照片
                val thinking1Photos = (thinking1Data["photoPaths"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (thinking1Photos.isNotEmpty()) {
                    SummarySubsection("照片") {
                        PhotoGrid(photoPaths = thinking1Photos)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 思考题二汇总
            SummarySection(
                title = "4. 思考题二",
                color = Color(0xFFFF9800),
                onEditClick = onNavigateToThinking2
            ) {
                SummarySubsection("题目") {
                    Text(
                        text = "通过亲身体验,感受茶文化,你觉得茶为什么可以成为\"中国文化名片\"?",
                        fontSize = 16.sp,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                val answer2 = thinking2Data["answer"] as? String ?: ""
                if (answer2.isNotEmpty()) {
                    SummarySubsection("答案") {
                        SummaryTextContent(answer2)
                    }
                }
                
                // 照片
                val thinking2Photos = (thinking2Data["photoPaths"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (thinking2Photos.isNotEmpty()) {
                    SummarySubsection("照片") {
                        PhotoGrid(photoPaths = thinking2Photos)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 茶助教记录汇总
            SummarySection(
                title = "5. 茶助教记录",
                color = Color(0xFF9C27B0),
                onEditClick = { },
                showEditButton = false  // 不显示编辑按钮
            ) {
                // 统计信息
                SummarySubsection("问答统计") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        SummaryStatCard("对话轮次", "${chatMessages.size / 2}", Color(0xFF4CAF50))
                        SummaryStatCard("学生提问", "${studentQuestions.size}", Color(0xFF2196F3))
                        SummaryStatCard("AI回答", "${chatMessages.size - studentQuestions.size}", Color(0xFF9C27B0))
                    }
                }

                // 学生提问列表
                if (studentQuestions.isNotEmpty()) {
                    SummarySubsection("学生提问列表") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            studentQuestions.forEachIndexed { index, question ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5DC)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF9C27B0),
                                            modifier = Modifier.width(30.dp)
                                        )
                                        Text(
                                            text = question,
                                            fontSize = 15.sp,
                                            color = Color(0xFF424242),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 完整对话记录
                if (chatMessages.isNotEmpty()) {
                    SummarySubsection("完整对话记录") {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            chatMessages.forEachIndexed { index, message ->
                                val isUser = message.role == "user"
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) Color(0xFFE8F5E9) else Color(0xFFF3E5F5)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // 角色标签
                                        Text(
                                            text = if (isUser) "👤 学生" else "🤖 AI助手",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUser) Color(0xFF2E7D32) else Color(0xFF7B1FA2),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        // 消息内容
                                        Text(
                                            text = message.content,
                                            fontSize = 15.sp,
                                            color = Color(0xFF212121),
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    color: Color,
    onEditClick: () -> Unit,
    showEditButton: Boolean = true,  // 默认显示编辑按钮
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                // 只在showEditButton为true时显示编辑按钮
                if (showEditButton) {
                    TextButton(
                        onClick = onEditClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = color
                        )
                    ) {
                        Text("编辑", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 15.dp),
                color = color.copy(alpha = 0.3f)
            )
            
            content()
        }
    }
}

@Composable
fun SummarySubsection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        content()
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = value.ifEmpty { "未填写" },
            fontSize = 16.sp,
            color = Color(0xFF424242),
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun SummaryTextContent(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Text(
            text = text.ifEmpty { "未填写" },
            fontSize = 16.sp,
            color = Color(0xFF424242),
            modifier = Modifier.padding(15.dp),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun SummaryTable(
    headers: List<String>,
    rows: List<List<String>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
    ) {
        // 表头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .border(1.dp, Color(0xFFBDBDBD))
        ) {
            headers.forEachIndexed { index, header ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .border(1.dp, Color(0xFFBDBDBD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = header,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
            }
        }
        
        // 数据行
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBDBDBD))
            ) {
                row.forEachIndexed { index, cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .border(1.dp, Color(0xFFBDBDBD)),
                        contentAlignment = if (index == 0) Alignment.Center else Alignment.TopStart
                    ) {
                        Text(
                            text = cell.ifEmpty { "-" },
                            fontSize = 14.sp,
                            color = Color(0xFF424242),
                            maxLines = 5,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoGrid(photoPaths: List<String>) {
    if (photoPaths.isEmpty()) {
        Text(
            text = "暂无照片",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.padding(vertical = 10.dp)
        )
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(photoPaths) { index, photoPath ->
                Box(
                    modifier = Modifier.size(120.dp)
                ) {
                    val bitmap = remember(photoPath) {
                        try {
                            BitmapFactory.decodeFile(photoPath)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "照片 ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RowScope.SummaryStatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            color.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
        }
    }
}
