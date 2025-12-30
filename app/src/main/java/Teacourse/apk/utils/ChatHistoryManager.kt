package Teacourse.apk.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * 聊天历史记录管理器
 * 负责保存和加载聊天历史记录
 */
class ChatHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "chat_history",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_CHAT_HISTORY = "chat_messages"          // 临时历史（显示用）
        private const val KEY_PERMANENT_HISTORY = "chat_messages_permanent"  // 永久历史（汇总用）
        private const val MAX_HISTORY_SIZE = 100 // 最多保存100条消息
    }

    /**
     * 保存聊天消息列表（临时历史）
     */
    fun saveChatMessages(messages: List<ChatMessage>) {
        saveMessagesToPrefs(messages, KEY_CHAT_HISTORY, "临时历史")
    }

    /**
     * 保存聊天消息列表到永久历史（汇总用）
     * 直接追加所有新消息到永久历史末尾
     */
    fun saveChatMessagesPermanent(newMessages: List<ChatMessage>) {
        if (newMessages.isEmpty()) return

        // 先读取已有的永久历史
        val existingMessages = loadChatMessagesPermanent()

        // 直接追加：旧历史 + 新消息
        val allMessages = existingMessages + newMessages

        // 限制最大数量（保留最新的100条）
        val finalMessages = if (allMessages.size > MAX_HISTORY_SIZE) {
            allMessages.takeLast(MAX_HISTORY_SIZE)
        } else {
            allMessages
        }

        saveMessagesToPrefs(finalMessages, KEY_PERMANENT_HISTORY, "永久历史")
        android.util.Log.d("ChatHistoryManager", "永久历史保存 ${newMessages.size} 条新消息，总计 ${finalMessages.size} 条")
    }

    /**
     * 追加单条消息到永久历史
     */
    fun appendMessageToPermanent(message: ChatMessage) {
        val existingMessages = loadChatMessagesPermanent()
        val allMessages = existingMessages + message

        val finalMessages = if (allMessages.size > MAX_HISTORY_SIZE) {
            allMessages.takeLast(MAX_HISTORY_SIZE)
        } else {
            allMessages
        }

        saveMessagesToPrefs(finalMessages, KEY_PERMANENT_HISTORY, "永久历史")
        android.util.Log.d("ChatHistoryManager", "永久历史追加1条消息，总计 ${finalMessages.size} 条")
    }

    /**
     * 内部方法：保存消息到指定的key
     */
    private fun saveMessagesToPrefs(messages: List<ChatMessage>, key: String, logTag: String) {
        try {
            val jsonArray = JSONArray()
            messages.forEach { message ->
                val jsonObject = JSONObject().apply {
                    put("role", message.role)
                    put("content", message.content)
                }
                jsonArray.put(jsonObject)
            }

            prefs.edit()
                .putString(key, jsonArray.toString())
                .apply()

            android.util.Log.d("ChatHistoryManager", "保存${logTag}：${messages.size} 条消息")
        } catch (e: Exception) {
            android.util.Log.e("ChatHistoryManager", "保存${logTag}失败", e)
        }
    }

    /**
     * 加载聊天消息列表（临时历史）
     */
    fun loadChatMessages(): List<ChatMessage> {
        return loadMessagesFromPrefs(KEY_CHAT_HISTORY, "临时历史")
    }

    /**
     * 加载聊天消息列表（永久历史，用于汇总）
     */
    fun loadChatMessagesPermanent(): List<ChatMessage> {
        return loadMessagesFromPrefs(KEY_PERMANENT_HISTORY, "永久历史")
    }

    /**
     * 内部方法：从指定的key加载消息
     */
    private fun loadMessagesFromPrefs(key: String, logTag: String): List<ChatMessage> {
        return try {
            val jsonString = prefs.getString(key, null)
            if (jsonString.isNullOrEmpty()) {
                android.util.Log.d("ChatHistoryManager", "没有保存的${logTag}")
                return emptyList()
            }

            val jsonArray = JSONArray(jsonString)
            val messages = mutableListOf<ChatMessage>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val role = jsonObject.getString("role")
                val content = jsonObject.getString("content")
                messages.add(ChatMessage(role, content))
            }

            android.util.Log.d("ChatHistoryManager", "加载了${logTag}：${messages.size} 条消息")
            messages
        } catch (e: Exception) {
            android.util.Log.e("ChatHistoryManager", "加载${logTag}失败", e)
            emptyList()
        }
    }

    /**
     * 清除临时聊天历史（不影响永久历史）
     */
    fun clearChatHistory() {
        prefs.edit()
            .remove(KEY_CHAT_HISTORY)
            .apply()
        android.util.Log.d("ChatHistoryManager", "清除了临时聊天历史（永久历史保留）")
    }

    /**
     * 清除永久聊天历史（仅用于重置等特殊情况）
     */
    fun clearPermanentHistory() {
        prefs.edit()
            .remove(KEY_PERMANENT_HISTORY)
            .apply()
        android.util.Log.d("ChatHistoryManager", "清除了永久聊天历史")
    }

    /**
     * 获取临时历史消息的数量
     */
    fun getHistorySize(): Int {
        return getHistorySizeFromKey(KEY_CHAT_HISTORY)
    }

    /**
     * 获取永久历史消息的数量
     */
    fun getPermanentHistorySize(): Int {
        return getHistorySizeFromKey(KEY_PERMANENT_HISTORY)
    }

    /**
     * 内部方法：从指定key获取历史消息数量
     */
    private fun getHistorySizeFromKey(key: String): Int {
        val jsonString = prefs.getString(key, null)
        return if (jsonString.isNullOrEmpty()) {
            0
        } else {
            try {
                JSONArray(jsonString).length()
            } catch (e: Exception) {
                0
            }
        }
    }

    /**
     * 将聊天历史转换为 JSONArray，用于提交到服务器（使用永久历史）
     */
    fun getChatHistoryAsJson(): JSONArray {
        val messages = loadChatMessagesPermanent()  // 使用永久历史
        val jsonArray = JSONArray()
        messages.forEach { message ->
            val jsonObject = JSONObject().apply {
                put("role", message.role)
                put("content", message.content)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    /**
     * 获取学生向助手提问的所有问题（仅用户消息，使用永久历史）
     */
    fun getUserQuestions(): List<String> {
        return loadChatMessagesPermanent()  // 使用永久历史
            .filter { it.role == "user" }
            .map { it.content }
    }

    /**
     * 获取格式化的聊天历史文本，便于教师阅读（使用永久历史）
     */
    fun getFormattedChatHistory(context: Context): String {
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

        val messages = loadChatMessagesPermanent()  // 使用永久历史
        if (messages.isEmpty()) {
            sb.appendLine("暂无聊天记录")
        } else {
            messages.forEachIndexed { index, message ->
                val role = if (message.role == "user") "学生" else "AI助手"
                sb.appendLine("[$role]")
                sb.appendLine(message.content)
                sb.appendLine()
                if (index < messages.size - 1) {
                    sb.appendLine("---")
                }
            }
        }

        return sb.toString()
    }
}

// 数据类定义
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)
