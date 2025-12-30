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
}

// 数据类定义
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String
)
