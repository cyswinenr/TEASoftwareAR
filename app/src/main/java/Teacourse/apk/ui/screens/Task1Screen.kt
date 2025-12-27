package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun Task1Screen(onBackClick: () -> Unit) {
    // 表格数据状态
    var dryTeaColor by remember { mutableStateOf("") }
    var dryTeaAroma by remember { mutableStateOf("") }
    var dryTeaShape by remember { mutableStateOf("") }
    var dryTeaTaste by remember { mutableStateOf("") }
    
    var teaLiquorColor by remember { mutableStateOf("") }
    var teaLiquorAroma by remember { mutableStateOf("") }
    var teaLiquorShape by remember { mutableStateOf("") }
    var teaLiquorTaste by remember { mutableStateOf("") }
    
    var spentLeavesColor by remember { mutableStateOf("") }
    var spentLeavesAroma by remember { mutableStateOf("") }
    var spentLeavesShape by remember { mutableStateOf("") }
    var spentLeavesTaste by remember { mutableStateOf("") }
    
    // 输入字段状态
    var waterTemperature by remember { mutableStateOf("") }
    var brewingDuration by remember { mutableStateOf("") }
    var teaCategory by remember { mutableStateOf("") }
    var teaName by remember { mutableStateOf("") }
    var teacherTeaName by remember { mutableStateOf("") }
    
    // 思考题答案
    var reflectionAnswer by remember { mutableStateOf("") }
    
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
            // 标题、计时器开关和返回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标题
                Text(
                    text = "1.任务一:泡茶体验、品茶时刻",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                // 计时器开关和返回按钮
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
            
            // 第一部分：同款茶不同形态的感官记录
            Text(
                text = "(1)同款茶不同形态的感官记录",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 15.dp)
            )
            
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
                        TableHeaderCell("第一次冲泡", 0.25f)
                        TableHeaderCell("色泽(观看)", 0.25f)
                        TableHeaderCell("香气(轻嗅)", 0.25f)
                        TableHeaderCell("形状(观看)", 0.25f)
                        TableHeaderCell("滋味(品尝)", 0.25f)
                    }
                    
                    // 干茶行
                    TableDataRow(
                        "干茶",
                        dryTeaColor,
                        { dryTeaColor = it },
                        dryTeaAroma,
                        { dryTeaAroma = it },
                        dryTeaShape,
                        { dryTeaShape = it },
                        dryTeaTaste,
                        { dryTeaTaste = it }
                    )
                    
                    // 茶汤行
                    TableDataRow(
                        "茶汤",
                        teaLiquorColor,
                        { teaLiquorColor = it },
                        teaLiquorAroma,
                        { teaLiquorAroma = it },
                        teaLiquorShape,
                        { teaLiquorShape = it },
                        teaLiquorTaste,
                        { teaLiquorTaste = it }
                    )
                    
                    // 叶底行
                    TableDataRow(
                        "叶底",
                        spentLeavesColor,
                        { spentLeavesColor = it },
                        spentLeavesAroma,
                        { spentLeavesAroma = it },
                        spentLeavesShape,
                        { spentLeavesShape = it },
                        spentLeavesTaste,
                        { spentLeavesTaste = it }
                    )
                }
            }
            
            // 输入字段
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                InputField("冲泡的水温:", waterTemperature) { waterTemperature = it }
                InputField("每泡茶叶的时长:", brewingDuration) { brewingDuration = it }
                InputField("茶叶品类:", teaCategory) { teaCategory = it }
                InputField("茶品名:", teaName) { teaName = it }
                InputField("老师公布的茶品名:", teacherTeaName) { teacherTeaName = it }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 第二部分：思考题
            Text(
                text = "(2)品评其他组同类的茶滋味,有何异同?为什么?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 15.dp)
            )
            
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

@Composable
fun RowScope.TableHeaderCell(text: String, weightValue: Float) {
    Box(
        modifier = Modifier
            .weight(weightValue)
            .height(50.dp)
            .border(1.dp, Color(0xFFBDBDBD))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
fun TableDataRow(
    rowLabel: String,
    colorValue: String,
    onColorChange: (String) -> Unit,
    aromaValue: String,
    onAromaChange: (String) -> Unit,
    shapeValue: String,
    onShapeChange: (String) -> Unit,
    tasteValue: String,
    onTasteChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBDBDBD))
    ) {
        // 行标签
        Box(
            modifier = Modifier
                .weight(0.25f)
                .height(60.dp)
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color(0xFFBDBDBD))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rowLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
        }
        
        // 输入框
        TableInputCell(colorValue, onColorChange, 0.25f)
        TableInputCell(aromaValue, onAromaChange, 0.25f)
        TableInputCell(shapeValue, onShapeChange, 0.25f)
        TableInputCell(tasteValue, onTasteChange, 0.25f)
    }
}

@Composable
fun RowScope.TableInputCell(
    value: String,
    onValueChange: (String) -> Unit,
    weightValue: Float
) {
    Box(
        modifier = Modifier
            .weight(weightValue)
            .height(60.dp)
            .border(1.dp, Color(0xFFBDBDBD))
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242),
            modifier = Modifier.width(180.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
}

