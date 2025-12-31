package Teacourse.apk.utils

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天消息数据库实体
 * 对应数据库表：chat_messages
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    /**
     * 主键ID，自动生成
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 消息角色："user" 或 "assistant"
     */
    val role: String,

    /**
     * 消息内容
     */
    val content: String,

    /**
     * 时间戳（毫秒）
     */
    val timestamp: Long,

    /**
     * 会话ID（可选，用于按会话分组）
     * 例如：一节课可以使用同一个sessionId
     */
    val sessionId: String = "default"
)
