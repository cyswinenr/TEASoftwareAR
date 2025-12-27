package Teacourse.apk.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.content.Context
import android.widget.Toast
import java.io.File
import Teacourse.apk.R

@Composable
fun SplashScreen(onStartClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showClearDataDialog by remember { mutableStateOf(false) }
    
    // 清除所有数据
    fun clearAllData() {
        val prefsNames = listOf(
            "TeaCultureApp",
            "Task1Data",
            "Task2Data",
            "Thinking1Data",
            "Thinking2Data",
            "CreativeData"
        )
        
        // 清除所有 SharedPreferences 数据
        prefsNames.forEach { prefsName ->
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            // 先获取照片路径，然后清除数据
            val photoPaths = prefs.getStringSet("photoPaths", setOf()) ?: setOf()
            
            // 删除所有照片文件
            photoPaths.forEach { photoPath ->
                try {
                    val file = File(photoPath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // 忽略删除失败的文件
                }
            }
            
            // 清除 SharedPreferences
            prefs.edit().clear().apply()
        }
        
        // 删除所有照片目录中的文件（以防有遗漏）
        try {
            val picturesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            picturesDir?.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".jpg") || file.name.endsWith(".jpeg"))) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // 忽略删除失败的文件
        }
        
        Toast.makeText(context, "所有数据已清除", Toast.LENGTH_SHORT).show()
        showClearDataDialog = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.cover_photo),
            contentDescription = "封面照片",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 半透明遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )
        
        // 中间圆框内容
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            // 圆形背景框（半透明）
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.75f),
                                Color.White.copy(alpha = 0.65f)
                            )
                        )
                    )
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    // 标题文字，分行显示，带阴影
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "茶文化课程",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                ),
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "APP",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                ),
                                letterSpacing = 4.sp
                            )
                        )
                    }
                    
                    Button(
                        onClick = onStartClick,
                        modifier = Modifier
                            .width(200.dp)
                            .height(60.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(30.dp)
                    ) {
                        Text(
                            text = "开始使用",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
            }
        }
        
        // 清除数据按钮（左上角）
        IconButton(
            onClick = { showClearDataDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFFFF5252).copy(alpha = 0.9f),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "清除数据",
                modifier = Modifier.size(28.dp)
            )
        }
        
        // 版本号（右下角）
        Text(
            text = "v0.1",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(1f, 1f),
                    blurRadius = 3f
                )
            )
        )
        
        // 清除数据确认对话框
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = {
                    Text(
                        text = "清除所有数据",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                text = {
                    Text(
                        text = "确定要清除所有已填写的数据吗？此操作不可恢复。",
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { clearAllData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("确定清除", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearDataDialog = false },
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
    }
}

