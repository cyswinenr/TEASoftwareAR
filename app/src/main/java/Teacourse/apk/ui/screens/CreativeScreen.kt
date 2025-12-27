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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreativeScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("CreativeData", Context.MODE_PRIVATE)
    }
    
    // 答案状态 - 从 SharedPreferences 加载
    var answer by remember { 
        mutableStateOf(sharedPreferences.getString("answer", "") ?: "") 
    }
    
    // 照片列表状态 - 从 SharedPreferences 加载
    var photoPaths by remember {
        val savedPaths = sharedPreferences.getStringSet("photoPaths", setOf()) ?: setOf()
        mutableStateOf(savedPaths.toList())
    }
    
    // 保存数据函数
    fun saveData() {
        with(sharedPreferences.edit()) {
            putString("answer", answer)
            putStringSet("photoPaths", photoPaths.toSet())
            apply()
        }
        Toast.makeText(context, "数据保存成功！", Toast.LENGTH_SHORT).show()
    }
    
    // 创建临时照片文件
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    // 当前照片文件 URI
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    
    // 拍照启动器
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoFile != null) {
            val photoPath = currentPhotoFile!!.absolutePath
            photoPaths = photoPaths + photoPath
            saveData() // 自动保存照片路径
            Toast.makeText(context, "照片已保存", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 启动拍照（内部函数）
    fun startTakePicture() {
        try {
            val photoFile = createImageFile()
            currentPhotoFile = photoFile
            
            val photoURI = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri = photoURI
            
            takePictureLauncher.launch(photoURI)
        } catch (e: Exception) {
            Toast.makeText(context, "拍照失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 相机权限请求启动器
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startTakePicture()
        } else {
            Toast.makeText(context, "需要相机权限才能拍照", Toast.LENGTH_LONG).show()
        }
    }
    
    // 拍照按钮点击
    fun takePicture() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            startTakePicture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // 删除照片
    fun deletePhoto(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            photoPaths = photoPaths.filter { it != path }
            saveData()
            Toast.makeText(context, "照片已删除", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "删除失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 检查语音识别是否可用
    val isSpeechRecognitionAvailable = remember {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    // 语音识别结果处理
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null && results.isNotEmpty()) {
                val spokenText = results[0]
                // 将语音识别的结果追加到现有答案中
                answer = if (answer.isEmpty()) {
                    spokenText
                } else {
                    "$answer $spokenText"
                }
            }
        } else {
            Toast.makeText(context, "语音识别未完成", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 内部启动语音识别（已检查权限）
    fun startSpeechRecognitionInternal() {
        if (!isSpeechRecognitionAvailable) {
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请开始说话...")
        }
        
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "语音识别启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognitionInternal()
        } else {
            Toast.makeText(context, "需要麦克风权限才能使用语音输入", Toast.LENGTH_LONG).show()
        }
    }
    
    // 启动语音识别（动态检查权限）
    fun startSpeechRecognition() {
        // 动态检查权限状态
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            startSpeechRecognitionInternal()
        } else {
            // 请求权限
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
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
            // 标题、保存按钮和返回按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标题
                Text(
                    text = "5.创意题",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
                
                // 保存按钮和返回按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 保存按钮
                    Button(
                        onClick = { saveData() },
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
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
                            containerColor = Color(0xFFBA68C8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("返回", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 题目内容
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
                    Text(
                        text = "如果让你们讲述或介绍中国茶文化,你会如何向别人介绍?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "(形式不限,可用文字、绘画、诗歌、宣传语、茶文创作品等形式进行呈现)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // 答案输入区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // 输入框标题和功能按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "请输入您的创意内容：",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                        
                        // 功能按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 拍照按钮
                            IconButton(
                                onClick = { takePicture() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Color(0xFF9C27B0),
                                        shape = RoundedCornerShape(28.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "拍照",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            
                            // 语音输入按钮
                            if (isSpeechRecognitionAvailable) {
                                IconButton(
                                    onClick = { startSpeechRecognition() },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            Color(0xFF9C27B0),
                                            shape = RoundedCornerShape(28.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "语音输入",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(15.dp))
                    
                    // 多行文本输入框
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        singleLine = false,
                        maxLines = 20,
                        minLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9C27B0),
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = {
                            Text(
                                text = "请在此输入您的创意内容，或点击右上角的麦克风图标进行语音输入...\n\n可以输入文字、诗歌、宣传语等内容",
                                fontSize = 16.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 照片上传区域
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
                    Text(
                        text = "照片上传：",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    
                    if (photoPaths.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "点击上方相机按钮拍照，照片将显示在这里",
                                fontSize = 16.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    } else {
                        // 显示照片网格
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(15.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(photoPaths) { index, photoPath ->
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                ) {
                                    // 显示照片
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
                                        
                                        // 删除按钮
                                        IconButton(
                                            onClick = { deletePhoto(photoPath) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(32.dp)
                                                .background(
                                                    Color(0xCC000000),
                                                    RoundedCornerShape(16.dp)
                                                )
                                        ) {
                                            Text(
                                                text = "×",
                                                fontSize = 20.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 温馨提示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF9C4)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "温馨提示:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "小组交流、讨论,完成工作纸,完成后上交老师。",
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

