package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import kotlinx.coroutines.delay

@Composable
fun Task2Screen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("Task2Data", Context.MODE_PRIVATE)
    }
    
    // 表格数据状态 - 从 SharedPreferences 加载
    var teaName by remember { mutableStateOf(sharedPreferences.getString("teaName", "") ?: "") }
    var waterTemperature by remember { mutableStateOf(sharedPreferences.getString("waterTemperature", "") ?: "") }
    var steepingDuration by remember { mutableStateOf(sharedPreferences.getString("steepingDuration", "") ?: "") }
    var teaColor by remember { mutableStateOf(sharedPreferences.getString("teaColor", "") ?: "") }
    var teaAroma by remember { mutableStateOf(sharedPreferences.getString("teaAroma", "") ?: "") }
    var teaTaste by remember { mutableStateOf(sharedPreferences.getString("teaTaste", "") ?: "") }
    
    // 第二部分状态
    var meetsExpectation by remember { mutableStateOf(sharedPreferences.getBoolean("meetsExpectation", false)) }
    var notMeetsExpectation by remember { mutableStateOf(sharedPreferences.getBoolean("notMeetsExpectation", false)) }
    var reflectionAnswer by remember { mutableStateOf(sharedPreferences.getString("reflectionAnswer", "") ?: "") }
    
    // 计时器状态
    var showTimer by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) } // 毫秒
    
    // 计时器逻辑
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(100)
            elapsedTime += 100
        }
    }
    
    // 格式化时间显示
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (millis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
    }
    
    // 保存数据函数
    fun saveData() {
        with(sharedPreferences.edit()) {
            // 保存表格数据
            putString("teaName", teaName)
            putString("waterTemperature", waterTemperature)
            putString("steepingDuration", steepingDuration)
            putString("teaColor", teaColor)
            putString("teaAroma", teaAroma)
            putString("teaTaste", teaTaste)
            
            // 保存第二部分数据
            putBoolean("meetsExpectation", meetsExpectation)
            putBoolean("notMeetsExpectation", notMeetsExpectation)
            putString("reflectionAnswer", reflectionAnswer)
            
            apply()
        }
        Toast.makeText(context, "数据保存成功！", Toast.LENGTH_SHORT).show()
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
            // 标题、计时器开关、保存按钮和返回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标题
                Text(
                    text = "2.任务二:泡出你心中的那杯茶",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                // 计时器开关、保存按钮和返回按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 计时器开关
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "计时器",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242)
                        )
                        Switch(
                            checked = showTimer,
                            onCheckedChange = { showTimer = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF4CAF50),
                                checkedTrackColor = Color(0xFF81C784)
                            )
                        )
                    }
                    
                    // 保存按钮
                    Button(
                        onClick = { saveData() },
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF388E3C)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // 返回按钮
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("返回", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // 计时器控件（如果显示）
            if (showTimer) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 时间显示
                        Text(
                            text = formatTime(elapsedTime),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        
                        // 按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            // 开始/暂停按钮
                            Button(
                                onClick = { isRunning = !isRunning },
                                modifier = Modifier.height(45.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isRunning) "暂停" else "开始",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 重置按钮
                            Button(
                                onClick = {
                                    isRunning = false
                                    elapsedTime = 0L
                                },
                                modifier = Modifier.height(45.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF81C784)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("重置", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 第一部分：第二次冲泡的关键因素控制及茶汤的特点记录
            Text(
                text = "(1)第二次冲泡的关键因素控制及茶汤的特点记录 (茶品名: _______)",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 15.dp)
            )
            
            // 茶品名输入
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "茶品名:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                OutlinedTextField(
                    value = teaName,
                    onValueChange = { teaName = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF81C784)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    singleLine = true
                )
            }
            
            // 表格
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(1.dp)
                ) {
                    // 表头
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0xFFBDBDBD))
                    ) {
                        TableHeaderCell("冲泡的水温", 0.2f)
                        TableHeaderCell("出汤的时长", 0.2f)
                        TableHeaderCell("茶汤的色泽(观看)", 0.2f)
                        TableHeaderCell("茶汤的香气(轻嗅)", 0.2f)
                        TableHeaderCell("茶汤的滋味(品尝)", 0.2f)
                    }
                    
                    // 数据行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFBDBDBD))
                    ) {
                        // 水温输入（带°C单位）
                        Box(
                            modifier = Modifier
                                .weight(0.2f)
                                .height(60.dp)
                                .border(1.dp, Color(0xFFBDBDBD))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedTextField(
                                    value = waterTemperature,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                            waterTemperature = newValue
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                Text(
                                    text = "°C",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                        
                        TableInputCell(steepingDuration, { steepingDuration = it }, 0.2f)
                        TableInputCell(teaColor, { teaColor = it }, 0.2f)
                        TableInputCell(teaAroma, { teaAroma = it }, 0.2f)
                        TableInputCell(teaTaste, { teaTaste = it }, 0.2f)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 第二部分：思考题
            Text(
                text = "(2)现冲泡的茶滋味是否符合心中的? 你觉得符合预期口/不符合预期口的 关键点在哪里?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 15.dp)
            )
            
            // 复选框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = meetsExpectation,
                        onCheckedChange = { 
                            meetsExpectation = it
                            if (it) notMeetsExpectation = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )
                    Text(
                        text = "符合预期",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = notMeetsExpectation,
                        onCheckedChange = { 
                            notMeetsExpectation = it
                            if (it) meetsExpectation = false
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50)
                        )
                    )
                    Text(
                        text = "不符合预期",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                }
            }
            
            // 答案输入框
            OutlinedTextField(
                value = reflectionAnswer,
                onValueChange = { reflectionAnswer = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                maxLines = 10
            )
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

