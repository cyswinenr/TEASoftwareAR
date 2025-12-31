package Teacourse.apk.utils

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 聊天消息数据访问对象
 * 定义对 chat_messages 表的所有数据库操作
 */
@Dao
interface ChatMessageDao {

    /**
     * 获取所有消息，按时间戳倒序排列（最新的在前）
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    /**
     * 获取最近的消息，按时间戳倒序
     * @param limit 限制返回的条数
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<ChatMessageEntity>

    /**
     * 获取指定会话的所有消息
     * @param sessionId 会话ID
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySession(sessionId: String): List<ChatMessageEntity>

    /**
     * 获取所有用户消息（role = 'user'）
     */
    @Query("SELECT * FROM chat_messages WHERE role = 'user' ORDER BY timestamp DESC")
    suspend fun getUserMessages(): List<ChatMessageEntity>

    /**
     * 获取消息总数
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int

    /**
     * 插入单条消息
     * @return 插入后的行ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity): Long

    /**
     * 插入多条消息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    /**
     * 删除所有消息
     */
    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()

    /**
     * 删除指定会话的所有消息
     * @param sessionId 会话ID
     */
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: String)

    /**
     * 删除指定时间之前的消息（用于清理旧数据）
     * @param timestamp 时间戳
     */
    @Query("DELETE FROM chat_messages WHERE timestamp < :timestamp")
    suspend fun deleteMessagesBefore(timestamp: Long): Int
}
