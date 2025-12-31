package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import Teacourse.apk.utils.MoonshotApiService
import Teacourse.apk.utils.ChatHistoryManager
import Teacourse.apk.utils.ChatMessage

@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 聊天历史管理器
    val historyManager = remember { ChatHistoryManager(context) }

    // 对话历史消息（用于 API 调用）
    val historyMessages = remember { mutableStateListOf<JSONObject>() }

    // UI 显示的对话消息
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }

    // 输入框状态
    var inputText by remember { mutableStateOf("") }

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 显示清除历史确认对话框
    var showClearDialog by remember { mutableStateOf(false) }

    // 用于区分加载历史和新增消息
    var initialLoadSize by remember { mutableStateOf(0) }
    var hasLoadedInitial by remember { mutableStateOf(false) }

    // API 服务
    val apiService = remember { MoonshotApiService() }

    // 首次加载时，从本地存储加载历史记录
    LaunchedEffect(Unit) {
        val savedMessages = historyManager.loadChatMessages()
        if (savedMessages.isNotEmpty()) {
            chatMessages.clear()
            chatMessages.addAll(savedMessages)

            // 同时恢复 API 历史消息
            historyMessages.clear()
            savedMessages.forEach { message ->
                historyMessages.add(JSONObject().apply {
                    put("role", message.role)
                    put("content", message.content)
                })
            }

            // 记录初始加载的大小
            initialLoadSize = chatMessages.size
            hasLoadedInitial = true

            // 滚动到最新消息
            if (chatMessages.isNotEmpty()) {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        } else {
            hasLoadedInitial = true
        }
    }

    // 当有新消息时，滚动到底部并保存历史记录
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty() && hasLoadedInitial) {
            // 保存到临时历史（显示用）
            historyManager.saveChatMessages(chatMessages.toList())

            // 只有当消息数量超过初始加载大小时，才追加到永久历史
            if (chatMessages.size > initialLoadSize) {
                val lastMessage = chatMessages.last()
                historyManager.appendMessageToPermanent(lastMessage)
            }

            // 滚动到底部
            coroutineScope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }
    
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
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E7D32))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 返回按钮
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // 标题
                Text(
                    text = "智能体问答",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // 清除历史记录按钮
                IconButton(
                    onClick = {
                        showClearDialog = true
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "清除历史",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 对话列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        // 欢迎消息
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "👋 你好！",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "我是茶文化课程的学习助手",
                                    fontSize = 20.sp,
                                    color = Color(0xFF424242),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "有什么关于茶文化的问题，尽管问我吧！",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(chatMessages) { index, message ->
                        ChatMessageItem(
                            message = message,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // 加载指示器
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE8F5E9)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color(0xFF2E7D32),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "正在思考...",
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 输入区域
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 输入框
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "输入您的问题...",
                                fontSize = 16.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF81C784),
                            focusedTextColor = Color(0xFF212121),
                            unfocusedTextColor = Color(0xFF212121)
                        ),
                        maxLines = 5,
                        singleLine = false
                    )
                    
                    // 发送按钮
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val userMessage = inputText.trim()
                                inputText = ""

                                // 添加用户消息到 UI
                                chatMessages.add(ChatMessage("user", userMessage))

                                // 发送流式请求
                                isLoading = true
                                var assistantMessageIndex = -1  // 用于跟踪助手消息的位置

                                apiService.chatStream(
                                    input = userMessage,
                                    historyMessages = historyMessages,
                                    onChunk = { chunk ->
                                        // 第一次收到数据时，添加助手消息到 UI
                                        if (assistantMessageIndex == -1) {
                                            assistantMessageIndex = chatMessages.size
                                            chatMessages.add(ChatMessage("assistant", chunk))
                                        } else {
                                            // 后续增量更新
                                            if (chatMessages.size > assistantMessageIndex) {
                                                val currentMessage = chatMessages[assistantMessageIndex]
                                                chatMessages[assistantMessageIndex] = ChatMessage(
                                                    currentMessage.role,
                                                    currentMessage.content + chunk
                                                )
                                            }
                                        }
                                    },
                                    onComplete = { fullContent ->
                                        isLoading = false
                                        // 如果还没有添加到 UI，现在添加
                                        if (assistantMessageIndex == -1) {
                                            assistantMessageIndex = chatMessages.size
                                            chatMessages.add(ChatMessage("assistant", fullContent))
                                        } else {
                                            // 确保最终内容正确
                                            if (chatMessages.size > assistantMessageIndex) {
                                                chatMessages[assistantMessageIndex] = ChatMessage("assistant", fullContent)
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        // 添加错误消息
                                        if (assistantMessageIndex == -1) {
                                            assistantMessageIndex = chatMessages.size
                                        }
                                        if (chatMessages.size > assistantMessageIndex) {
                                            chatMessages[assistantMessageIndex] = ChatMessage(
                                                "assistant",
                                                "抱歉，发生了错误：$error"
                                            )
                                        } else {
                                            chatMessages.add(ChatMessage("assistant", "抱歉，发生了错误：$error"))
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (isLoading || inputText.isBlank()) {
                            Color(0xFF9E9E9E)
                        } else {
                            Color(0xFF4CAF50)
                        }
                    ) {
                        when {
                            isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "发送",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 清除历史记录确认对话框
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = {
                    Text(
                        text = "清除历史记录",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "确定要清除聊天记录吗？",
                            fontSize = 16.sp,
                            color = Color(0xFF424242)
                        )
                        Text(
                            text = "• 清除当前页面的对话显示",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "• AI将重新开始，不记得之前的对话",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "• 汇总页面仍保留所有历史记录",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            chatMessages.clear()
                            historyMessages.clear()
                            historyManager.clearChatHistory()
                            initialLoadSize = 0  // 重置初始加载大小
                            showClearDialog = false
                        }
                    ) {
                        Text("确定", color = Color(0xFF2E7D32))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearDialog = false }
                    ) {
                        Text("取消", color = Color(0xFF757575))
                    }
                }
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val roleLabel = if (isUser) "我" else "茶助教"

    Row(
        modifier = modifier,
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth(if (isUser) 0.65f else 0.95f)
                .padding(horizontal = if (isUser) 40.dp else 8.dp)
        ) {
            // 角色标识标签行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    color = if (isUser) Color(0xFF2E7D32) else Color(0xFF4CAF50),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 0.dp
                    ),
                    modifier = Modifier.padding(
                        start = if (!isUser) 12.dp else 0.dp,
                        end = if (isUser) 12.dp else 0.dp,
                        top = 8.dp,
                        bottom = 4.dp
                    )
                ) {
                    Text(
                        text = roleLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        Color(0xFF4CAF50)
                    } else {
                        Color.White.copy(alpha = 0.95f)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    // 显示消息内容 - AI的回答可选择复制
                    if (isUser) {
                        Text(
                            text = message.content,
                            fontSize = 17.sp,
                            color = Color.White,
                            lineHeight = 26.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SelectionContainer {
                            Text(
                                text = message.content,
                                fontSize = 17.sp,
                                color = Color(0xFF212121),
                                lineHeight = 26.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

