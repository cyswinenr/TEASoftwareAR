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
        private const val KEY_CHAT_HISTORY = "chat_messages"
        private const val MAX_HISTORY_SIZE = 100 // 最多保存100条消息
    }

    /**
     * 保存聊天消息列表
     */
    fun saveChatMessages(messages: List<ChatMessage>) {
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
                .putString(KEY_CHAT_HISTORY, jsonArray.toString())
                .apply()

            android.util.Log.d("ChatHistoryManager", "保存了 ${messages.size} 条聊天消息")
        } catch (e: Exception) {
            android.util.Log.e("ChatHistoryManager", "保存聊天历史失败", e)
        }
    }

    /**
     * 加载聊天消息列表
     */
    fun loadChatMessages(): List<ChatMessage> {
        return try {
            val jsonString = prefs.getString(KEY_CHAT_HISTORY, null)
            if (jsonString.isNullOrEmpty()) {
                android.util.Log.d("ChatHistoryManager", "没有保存的聊天历史")
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

            android.util.Log.d("ChatHistoryManager", "加载了 ${messages.size} 条聊天消息")
            messages
        } catch (e: Exception) {
            android.util.Log.e("ChatHistoryManager", "加载聊天历史失败", e)
            emptyList()
        }
    }

    /**
     * 清除所有聊天历史
     */
    fun clearChatHistory() {
        prefs.edit().clear().apply()
        android.util.Log.d("ChatHistoryManager", "清除了所有聊天历史")
    }

    /**
     * 获取历史消息的数量
     */
    fun getHistorySize(): Int {
        val jsonString = prefs.getString(KEY_CHAT_HISTORY, null)
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
     * 将聊天历史转换为 JSONArray，用于提交到服务器
     */
    fun getChatHistoryAsJson(): JSONArray {
        val messages = loadChatMessages()
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
     * 获取学生向助手提问的所有问题（仅用户消息）
     */
    fun getUserQuestions(): List<String> {
        return loadChatMessages()
            .filter { it.role == "user" }
            .map { it.content }
    }

    /**
     * 获取格式化的聊天历史文本，便于教师阅读
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

        val messages = loadChatMessages()
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
