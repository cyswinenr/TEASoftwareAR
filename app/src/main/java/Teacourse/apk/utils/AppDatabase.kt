package Teacourse.apk.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 茶文化App数据库
 * 版本：1
 * 包含表：chat_messages
 */
@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取聊天消息DAO
     */
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        /**
         * 数据库名称
         */
        private const val DATABASE_NAME = "tea_culture_database"

        /**
         * 单例实例
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例（单例模式）
         * 使用双重检查锁定确保线程安全
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // 允许在主线程查询（仅用于测试，生产环境不建议）
                    // .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 关闭数据库连接（用于测试或清理）
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
