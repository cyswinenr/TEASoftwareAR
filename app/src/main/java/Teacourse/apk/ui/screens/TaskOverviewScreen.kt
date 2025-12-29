package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import Teacourse.apk.navigation.Screen
import Teacourse.apk.utils.DataSubmissionService

data class TaskItem(
    val title: String,
    val route: String,
    val color: Color
)

@Composable
fun TaskOverviewScreen(
    onTaskClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsPrefs = remember {
        context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
    }
    
    // 辅助函数：从URL解析IP和端口
    fun parseServerUrl(url: String): Pair<String, String> {
        return try {
            val regex = Regex("""http://([^:]+):(\d+)""")
            val matchResult = regex.find(url)
            if (matchResult != null) {
                val ip = matchResult.groupValues[1]
                val port = matchResult.groupValues[2]
                Pair(ip, port)
            } else {
                Pair("172.16.70.101", "8888")
            }
        } catch (e: Exception) {
            Pair("172.16.70.101", "8888")
        }
    }
    
    // 辅助函数：从IP和端口构建URL
    fun buildServerUrl(ip: String, port: String): String {
        return "http://$ip:$port"
    }
    
    // 默认服务器地址
    val defaultServerUrl = "http://172.16.70.101:8888"
    
    // 服务器地址状态
    var serverUrl by remember {
        mutableStateOf(
            settingsPrefs.getString("serverUrl", defaultServerUrl) ?: defaultServerUrl
        )
    }
    
    // 对话框状态
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitStatus by remember { mutableStateOf<String?>(null) }
    
    // 临时IP地址和端口（用于对话框输入）
    var tempIp by remember { 
        mutableStateOf(parseServerUrl(serverUrl).first)
    }
    var tempPort by remember { 
        mutableStateOf(parseServerUrl(serverUrl).second)
    }
    
    // 当对话框打开时，更新临时值
    LaunchedEffect(showServerConfigDialog) {
        if (showServerConfigDialog) {
            val (ip, port) = parseServerUrl(serverUrl)
            tempIp = ip
            tempPort = port
        }
    }
    
    // 超时处理：如果30秒没有响应，自动取消提交状态
    LaunchedEffect(isSubmitting) {
        if (isSubmitting) {
            kotlinx.coroutines.delay(30000) // 30秒超时
            if (isSubmitting) {
                isSubmitting = false
                submitStatus = "提交超时，请检查网络连接和服务器地址"
                Toast.makeText(context, "提交超时，请检查网络连接", Toast.LENGTH_LONG).show()
            }
        }
    }
    // 5个任务横向排列
    val tasks = listOf(
        // 任务一、二：绿色系（茶文化主题）
        TaskItem("任务一：泡茶体验、品茶时", Screen.Task1.route, Color(0xFF4CAF50)),
        TaskItem("任务二：泡出你心中的那杯茶", Screen.Task2.route, Color(0xFF66BB6A)),
        // 思考题一、二：橙色系（代表思考、智慧）
        TaskItem("思考题一", Screen.Thinking1.route, Color(0xFFFF9800)),
        TaskItem("思考题二", Screen.Thinking2.route, Color(0xFFFFB74D)),
        // 创意题：紫色系（代表创意、想象力）
        TaskItem("创意题", Screen.Creative.route, Color(0xFF9C27B0))
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5DC),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            // 标题和返回按钮在同一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标题
                Text(
                    text = "任务总览",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                // 按钮组（右上角）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 提交数据按钮
                    Button(
                        onClick = {
                            if (serverUrl.isEmpty() || !serverUrl.startsWith("http")) {
                                showServerConfigDialog = true
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        modifier = Modifier
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "提交数据",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "提交数据",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // 服务器配置按钮
                    IconButton(
                        onClick = { showServerConfigDialog = true },
                        modifier = Modifier
                            .size(50.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF757575)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "服务器配置",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // 查看汇总按钮
                    Button(
                        onClick = onSummaryClick,
                        modifier = Modifier
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "查看汇总",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // 返回按钮
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "返回",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // 任务横向滚动列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task.route) }
                    )
                }
            }
        }
        
        // 服务器配置对话框
        if (showServerConfigDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showServerConfigDialog = false
                    val (ip, port) = parseServerUrl(serverUrl)
                    tempIp = ip
                    tempPort = port
                },
                title = {
                    Text(
                        text = "服务器配置",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            text = "请输入服务器IP地址和端口号",
                            fontSize = 16.sp,
                            color = Color(0xFF424242)
                        )
                        
                        // IP地址输入框
                        OutlinedTextField(
                            value = tempIp,
                            onValueChange = { newValue ->
                                // 只允许数字、点和连字符
                                if (newValue.all { it.isDigit() || it == '.' || it == '-' }) {
                                    tempIp = newValue
                                }
                            },
                            label = { Text("IP地址", fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("172.16.70.101", fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF81C784)
                            )
                        )
                        
                        // 端口号输入框
                        OutlinedTextField(
                            value = tempPort,
                            onValueChange = { newValue ->
                                // 只允许数字
                                if (newValue.all { it.isDigit() }) {
                                    tempPort = newValue
                                }
                            },
                            label = { Text("端口号", fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("8888", fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF81C784)
                            )
                        )
                        
                        // 显示完整地址预览
                        Text(
                            text = "完整地址：http://$tempIp:$tempPort",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 5.dp)
                        )
                        
                        if (serverUrl.isNotEmpty()) {
                            Text(
                                text = "当前地址：$serverUrl",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempIp.isNotEmpty() && tempPort.isNotEmpty()) {
                                // 验证IP地址格式（简单验证）
                                val ipParts = tempIp.split(".")
                                val isValidIp = ipParts.size == 4 && 
                                    ipParts.all { part -> 
                                        part.toIntOrNull()?.let { it in 0..255 } ?: false 
                                    }
                                
                                // 验证端口号
                                val isValidPort = tempPort.toIntOrNull()?.let { it in 1..65535 } ?: false
                                
                                if (isValidIp && isValidPort) {
                                    serverUrl = buildServerUrl(tempIp.trim(), tempPort.trim())
                                    settingsPrefs.edit().putString("serverUrl", serverUrl).apply()
                                    showServerConfigDialog = false
                                    Toast.makeText(context, "服务器地址已保存", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (!isValidIp) {
                                        Toast.makeText(context, "请输入有效的IP地址（例如：172.16.70.101）", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "请输入有效的端口号（1-65535）", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "请输入IP地址和端口号", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showServerConfigDialog = false
                            val (ip, port) = parseServerUrl(serverUrl)
                            tempIp = ip
                            tempPort = port
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text("取消", fontSize = 16.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
        
        // 提交数据对话框
        if (showSubmitDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isSubmitting) {
                        showSubmitDialog = false
                        submitStatus = null
                    }
                },
                title = {
                    Text(
                        text = "提交数据到服务器",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            text = "服务器地址：$serverUrl",
                            fontSize = 16.sp,
                            color = Color(0xFF424242)
                        )
                        
                        if (isSubmitting) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "正在提交数据...",
                                    fontSize = 16.sp,
                                    color = Color(0xFF424242)
                                )
                            }
                        }
                        
                        submitStatus?.let { status ->
                            Text(
                                text = status,
                                fontSize = 14.sp,
                                color = if (status.contains("成功")) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                        
                        if (!isSubmitting && submitStatus == null) {
                            Text(
                                text = "确定要提交所有数据到服务器吗？",
                                fontSize = 16.sp,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!isSubmitting) {
                        Button(
                            onClick = {
                                try {
                                    // 先验证必填字段
                                    val studentPrefs = context.getSharedPreferences("TeaCultureApp", Context.MODE_PRIVATE)
                                    val school = studentPrefs.getString("school", "") ?: ""
                                    val grade = studentPrefs.getString("grade", "") ?: ""
                                    val classNumber = studentPrefs.getString("classNumber", "") ?: ""
                                    val memberCount = studentPrefs.getInt("memberCount", 0)
                                    
                                    if (school.isEmpty()) {
                                        Toast.makeText(context, "请先填写学校信息", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (grade.isEmpty() || (grade != "高一" && grade != "高二")) {
                                        Toast.makeText(context, "请选择年级（高一或高二）", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (classNumber.isEmpty()) {
                                        Toast.makeText(context, "请填写班级号", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (memberCount == 0) {
                                        Toast.makeText(context, "请选择小组成员人数", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    
                                    isSubmitting = true
                                    submitStatus = null
                                    
                                    // 在IO线程执行数据收集和提交
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            android.util.Log.d("TaskOverview", "开始提交数据到: $serverUrl")
                                            val submissionService = DataSubmissionService(context)
                                            
                                            submissionService.submitData(
                                                serverUrl = serverUrl,
                                                onSuccess = { message ->
                                                    // 回调已经在主线程，直接更新UI
                                                    isSubmitting = false
                                                    submitStatus = "提交成功！"
                                                    Toast.makeText(context, "数据提交成功！", Toast.LENGTH_SHORT).show()
                                                    android.util.Log.d("TaskOverview", "提交成功: $message")
                                                },
                                                onError = { error ->
                                                    // 回调已经在主线程，直接更新UI
                                                    isSubmitting = false
                                                    submitStatus = "提交失败：$error"
                                                    Toast.makeText(context, "提交失败：$error", Toast.LENGTH_LONG).show()
                                                    android.util.Log.e("TaskOverview", "提交失败: $error")
                                                }
                                            )
                                        } catch (e: Exception) {
                                            // 切换到主线程更新UI
                                            launch(Dispatchers.Main) {
                                                isSubmitting = false
                                                submitStatus = "提交异常：${e.message}"
                                                Toast.makeText(context, "提交异常：${e.message}", Toast.LENGTH_LONG).show()
                                                android.util.Log.e("TaskOverview", "提交异常", e)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    submitStatus = "启动提交失败：${e.message}"
                                    Toast.makeText(context, "启动提交失败：${e.message}", Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isSubmitting && submitStatus == null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("确定提交", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9E9E9E)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("提交中...", fontSize = 16.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            if (!isSubmitting) {
                                showSubmitDialog = false
                                submitStatus = null
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text("关闭", fontSize = 16.sp)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    // 创建渐变颜色
    val gradientColors = when (task.color) {
        Color(0xFF4CAF50) -> listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
        Color(0xFF66BB6A) -> listOf(Color(0xFF66BB6A), Color(0xFF81C784))
        Color(0xFFFF9800) -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
        Color(0xFFFFB74D) -> listOf(Color(0xFFFFB74D), Color(0xFFFFCC80))
        Color(0xFF9C27B0) -> listOf(Color(0xFF9C27B0), Color(0xFFBA68C8))
        else -> listOf(task.color, task.color.copy(alpha = 0.8f))
    }
    
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(380.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = task.color.copy(alpha = 0.4f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 添加装饰性图标或数字
                Text(
                    text = when (task.title) {
                        "任务一：泡茶体验、品茶时" -> "①"
                        "任务二：泡出你心中的那杯茶" -> "②"
                        "思考题一" -> "💭"
                        "思考题二" -> "💡"
                        "创意题" -> "✨"
                        else -> "📋"
                    },
                    fontSize = 72.sp,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                Text(
                    text = task.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

