package Teacourse.apk.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 聊天历史记录管理器（Room数据库版本）
 * 负责保存和加载聊天历史记录
 *
 * 重要：所有公共方法保持与旧版本兼容，接口不变
 */
class ChatHistoryManager(context: Context) {

    // 保留旧的SharedPreferences用于数据迁移
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "chat_history",
        Context.MODE_PRIVATE
    )

    // Room数据库
    private val database = AppDatabase.getInstance(context)
    private val chatDao = database.chatMessageDao()

    // 旧的SharedPreferences Key（用于迁移）
    companion object {
        private const val KEY_CHAT_HISTORY = "chat_messages"
        private const val KEY_PERMANENT_HISTORY = "chat_messages_permanent"

        // Room版本不再有硬性限制，但为了UI性能建议限制加载数量
        private const val DEFAULT_LOAD_LIMIT = 500  // 默认加载最近500条
    }

    init {
        // 首次使用时，自动迁移旧数据
        migrateOldDataIfNeeded()
    }

    /**
     * 迁移旧的SharedPreferences数据到Room数据库
     * 只执行一次，迁移完成后会标记
     */
    private fun migrateOldDataIfNeeded() {
        val migrationKey = "room_migration_completed"
        val hasMigrated = prefs.getBoolean(migrationKey, false)

        if (!hasMigrated) {
            Log.d("ChatHistoryManager", "开始迁移旧数据到Room数据库...")
            try {
                // 使用runBlocking在非协程环境中执行suspend函数
                kotlinx.coroutines.runBlocking {
                    // 迁移临时历史
                    migrateKeyToRoom(KEY_CHAT_HISTORY)

                    // 迁移永久历史
                    migrateKeyToRoom(KEY_PERMANENT_HISTORY)
                }

                // 标记迁移完成
                prefs.edit().putBoolean(migrationKey, true).apply()
                Log.d("ChatHistoryManager", "旧数据迁移完成")
            } catch (e: Exception) {
                Log.e("ChatHistoryManager", "数据迁移失败", e)
            }
        }
    }

    /**
     * 迁移指定Key的数据到Room
     */
    private suspend fun migrateKeyToRoom(key: String) {
        val jsonString = prefs.getString(key, null)
        if (!jsonString.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                val entities = mutableListOf<ChatMessageEntity>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val role = jsonObject.getString("role")
                    val content = jsonObject.getString("content")

                    entities.add(
                        ChatMessageEntity(
                            role = role,
                            content = content,
                            timestamp = System.currentTimeMillis() - (jsonArray.length() - i) * 1000,
                            sessionId = "migrated_$key"
                        )
                    )
                }

                if (entities.isNotEmpty()) {
                    chatDao.insertAll(entities)
                    Log.d("ChatHistoryManager", "从 $key 迁移了 ${entities.size} 条消息")
                }
            } catch (e: Exception) {
                Log.e("ChatHistoryManager", "迁移 $key 失败", e)
            }
        }
    }

    /**
     * 保存聊天消息列表（临时历史）
     * 保持与旧版本接口兼容
     */
    suspend fun saveChatMessages(messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            // 转换为Entity
            val entities = messages.map { message ->
                ChatMessageEntity(
                    role = message.role,
                    content = message.content,
                    timestamp = System.currentTimeMillis(),
                    sessionId = "temporary"
                )
            }

            // 先清除旧的临时数据
            chatDao.clearSession("temporary")

            // 插入新数据
            chatDao.insertAll(entities)

            Log.d("ChatHistoryManager", "保存临时历史：${messages.size} 条消息")
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "保存临时历史失败", e)
        }
    }

    /**
     * 保存聊天消息列表到永久历史（汇总用）
     * 保持与旧版本接口兼容
     * 直接追加所有新消息到永久历史末尾
     */
    suspend fun saveChatMessagesPermanent(newMessages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        if (newMessages.isEmpty()) return@withContext

        try {
            // 转换为Entity
            val entities = newMessages.map { message ->
                ChatMessageEntity(
                    role = message.role,
                    content = message.content,
                    timestamp = System.currentTimeMillis(),
                    sessionId = "permanent"
                )
            }

            // 批量插入
            chatDao.insertAll(entities)

            Log.d("ChatHistoryManager", "永久历史保存 ${newMessages.size} 条新消息")
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "保存永久历史失败", e)
        }
    }

    /**
     * 追加单条消息到永久历史
     * 保持与旧版本接口兼容
     */
    suspend fun appendMessageToPermanent(message: ChatMessage) = withContext(Dispatchers.IO) {
        try {
            val entity = ChatMessageEntity(
                role = message.role,
                content = message.content,
                timestamp = System.currentTimeMillis(),
                sessionId = "permanent"
            )

            chatDao.insert(entity)

            Log.d("ChatHistoryManager", "永久历史追加1条消息")
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "追加消息失败", e)
        }
    }

    /**
     * 加载聊天消息列表（临时历史）
     * 保持与旧版本接口兼容
     */
    suspend fun loadChatMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entities = chatDao.getMessagesBySession("temporary")
            entities.map { it.toChatMessage() }
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "加载临时历史失败", e)
            emptyList()
        }
    }

    /**
     * 加载聊天消息列表（永久历史，用于汇总）
     * 保持与旧版本接口兼容
     */
    suspend fun loadChatMessagesPermanent(): List<ChatMessage> = withContext(Dispatchers.IO) {
        return@withContext try {
            val entities = chatDao.getMessagesBySession("permanent")
            entities.map { it.toChatMessage() }
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "加载永久历史失败", e)
            emptyList()
        }
    }

    /**
     * 清除临时聊天历史（不影响永久历史）
     * 保持与旧版本接口兼容
     */
    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        try {
            chatDao.clearSession("temporary")
            Log.d("ChatHistoryManager", "清除了临时聊天历史（永久历史保留）")
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "清除临时历史失败", e)
        }
    }

    /**
     * 清除永久聊天历史（仅用于重置等特殊情况）
     * 保持与旧版本接口兼容
     */
    suspend fun clearPermanentHistory() = withContext(Dispatchers.IO) {
        try {
            chatDao.clearSession("permanent")
            Log.d("ChatHistoryManager", "清除了永久聊天历史")
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "清除永久历史失败", e)
        }
    }

    /**
     * 获取临时历史消息的数量
     * 同步方法，保持与旧版本兼容
     */
    fun getHistorySize(): Int {
        return try {
            // 注意：Room的suspend函数不能在非协程中调用
            // 这里为了保持兼容性，使用runBlocking包装
            kotlinx.coroutines.runBlocking {
                chatDao.getMessagesBySession("temporary").size
            }
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "获取临时历史数量失败", e)
            0
        }
    }

    /**
     * 获取永久历史消息的数量
     * 同步方法，保持与旧版本兼容
     */
    fun getPermanentHistorySize(): Int {
        return try {
            kotlinx.coroutines.runBlocking {
                chatDao.getMessagesBySession("permanent").size
            }
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "获取永久历史数量失败", e)
            0
        }
    }

    /**
     * 将聊天历史转换为 JSONArray，用于提交到服务器（使用永久历史）
     * 同步方法，保持与旧版本兼容
     */
    fun getChatHistoryAsJson(): JSONArray {
        return try {
            // 使用runBlocking获取数据
            val messages = kotlinx.coroutines.runBlocking {
                chatDao.getMessagesBySession("permanent")
            }

            val jsonArray = JSONArray()
            messages.forEach { entity ->
                val jsonObject = JSONObject().apply {
                    put("role", entity.role)
                    put("content", entity.content)
                }
                jsonArray.put(jsonObject)
            }
            jsonArray
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "转换为JSON失败", e)
            JSONArray()
        }
    }

    /**
     * 获取学生向助手提问的所有问题（仅用户消息，使用永久历史）
     * 同步方法，保持与旧版本兼容
     */
    fun getUserQuestions(): List<String> {
        return try {
            val messages = kotlinx.coroutines.runBlocking {
                chatDao.getMessagesBySession("permanent")
            }

            messages
                .filter { it.role == "user" }
                .map { it.content }
        } catch (e: Exception) {
            Log.e("ChatHistoryManager", "获取用户问题失败", e)
            emptyList()
        }
    }

    /**
     * 获取格式化的聊天历史文本，便于教师阅读（使用永久历史）
     * 同步方法，保持与旧版本兼容
     */
    suspend fun getFormattedChatHistory(context: Context): String = withContext(Dispatchers.IO) {
        val studentPrefs = context.getSharedPreferences("TeaCultureApp", Context.MODE_PRIVATE)
        val school = studentPrefs.getString("school", "") ?: ""
        val grade = studentPrefs.getString("grade", "") ?: ""
        val classNumber = studentPrefs.getString("classNumber", "") ?: ""
        val groupNumber = studentPrefs.getInt("groupNumber", 0)
        val date = studentPrefs.getString("date", "") ?: ""

        val sb = StringBuilder()
        sb.appendLine("=== 茶文化课程 - 智能体问答记录 ===")
        sb.appendLine()
        sb.appendLine("【学生信息】")
        sb.appendLine("学校: $school")
        sb.appendLine("年级: $grade")
        sb.appendLine("班级: $classNumber")
        sb.appendLine("小组编号: $groupNumber")
        sb.appendLine("日期: $date")
        sb.appendLine()
        sb.appendLine("=== 问答记录 ===")
        sb.appendLine()

        val messages = chatDao.getMessagesBySession("permanent")
        if (messages.isEmpty()) {
            sb.appendLine("暂无聊天记录")
        } else {
            messages.forEachIndexed { index, entity ->
                val role = if (entity.role == "user") "学生" else "AI助手"
                sb.appendLine("[$role]")
                sb.appendLine(entity.content)
                sb.appendLine()
                if (index < messages.size - 1) {
                    sb.appendLine("---")
                }
            }
        }

        return@withContext sb.toString()
    }

    /**
     * ChatMessageEntity 转换为 ChatMessage
     */
    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        return ChatMessage(role, content)
    }
}

// 数据类定义（保持不变）
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)
