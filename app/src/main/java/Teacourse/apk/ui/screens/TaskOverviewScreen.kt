package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    
    // 服务器地址状态
    var serverUrl by remember {
        mutableStateOf(
            settingsPrefs.getString("serverUrl", "http://192.168.3.16:5000") ?: "http://192.168.3.16:5000"
        )
    }
    
    // 对话框状态
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitStatus by remember { mutableStateOf<String?>(null) }
    
    // 临时服务器地址（用于对话框输入）
    var tempServerUrl by remember { mutableStateOf(serverUrl) }
    
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
    // 调整顺序，让思考题一在第二行（3列布局）
    // 第一行：任务一、任务二、（空）
    // 第二行：思考题一、思考题二、（空）
    // 第三行：创意题、（空）、（空）
    val tasks = listOf(
        // 任务一、二：绿色系（茶文化主题）
        TaskItem("任务一：泡茶体验、品茶时", Screen.Task1.route, Color(0xFF4CAF50)),
        TaskItem("任务二：泡出你心中的那杯茶", Screen.Task2.route, Color(0xFF4CAF50)),
        // 占位项（透明，不显示）
        TaskItem("", "", Color.Transparent),
        // 思考题一、二：橙色系（代表思考、智慧）- 放在第二行
        TaskItem("思考题一", Screen.Thinking1.route, Color(0xFFFF9800)),
        TaskItem("思考题二", Screen.Thinking2.route, Color(0xFFFF9800)),
        // 占位项（透明，不显示）
        TaskItem("", "", Color.Transparent),
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
                            containerColor = Color(0xFF81C784)
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
            
            // 任务网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    if (task.route.isNotEmpty()) {
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task.route) }
                        )
                    } else {
                        // 占位项，不显示
                        Spacer(modifier = Modifier.fillMaxWidth().height(200.dp))
                    }
                }
            }
        }
        
        // 服务器配置对话框
        if (showServerConfigDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showServerConfigDialog = false
                    tempServerUrl = serverUrl
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
                            text = "请输入服务器地址（例如：http://192.168.3.16:5000）",
                            fontSize = 16.sp,
                            color = Color(0xFF424242)
                        )
                        OutlinedTextField(
                            value = tempServerUrl,
                            onValueChange = { tempServerUrl = it },
                            label = { Text("服务器地址", fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("http://192.168.3.16:5000", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF81C784)
                            )
                        )
                        if (serverUrl.isNotEmpty()) {
                            Text(
                                text = "当前地址：$serverUrl",
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempServerUrl.isNotEmpty() && 
                                (tempServerUrl.startsWith("http://") || tempServerUrl.startsWith("https://"))) {
                                serverUrl = tempServerUrl.trim()
                                settingsPrefs.edit().putString("serverUrl", serverUrl).apply()
                                showServerConfigDialog = false
                                Toast.makeText(context, "服务器地址已保存", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "请输入有效的服务器地址（以http://或https://开头）", Toast.LENGTH_LONG).show()
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
                            tempServerUrl = serverUrl
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = task.color
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = task.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }
    }
}

