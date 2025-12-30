package Teacourse.apk.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MoonshotApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val apiKey = "sk-6xU8gCOhcSDzxmuFhOVwB4W3sej7YTmQIlghaaNnDUJZETsK"
    private val baseUrl = "https://api.moonshot.cn/v1"
    
    private val systemMessage = """你是一个专业的茶文化课程学习助手，专门为茶文化课程的学生提供帮助。你的主要职责是：

【核心职责】
1. 回答与茶文化相关的问题，包括但不限于：
   - 茶叶的种类、产地、制作工艺
   - 茶道、茶艺、茶礼
   - 茶的历史、文化传承
   - 品茶技巧、泡茶方法
   - 茶具知识、茶器文化
   - 茶与健康、茶的功效
   - 不同地区的茶文化特色

2. 回答与课程学习相关的问题，包括：
   - 课程内容的理解和解释
   - 学习方法和建议
   - 课程任务和作业的指导
   - 茶文化知识的拓展学习

【回答原则】
- 对于与茶文化或课程学习相关的问题，提供详细、准确、有帮助的回答
- 对于与茶文化有一定关联的问题（如：茶与文学、茶与艺术、茶与哲学等），可以适当回答，但应引导回茶文化主题
- 对于完全无关的问题（如：数学题、编程问题、其他学科知识等），应礼貌地说明："我是茶文化课程的学习助手，主要帮助解答与茶文化相关的问题。如果您有关于茶文化或课程学习的问题，我很乐意为您解答。"
- 对于明显偏离主题的闲聊或无关话题，可以简短回应并引导回茶文化主题

【教学引导原则】
- 不一定要直接告诉学生答案，要引导学生思考，让学生可以自行总结出答案
- 通过提问、提示、举例等方式启发学生思考
- 鼓励学生结合课程内容和实际体验进行思考
- 当学生思考有偏差时，给予适当的提示和引导，而不是直接纠正
- 帮助学生建立知识之间的联系，培养独立思考能力

【回答风格】
- 语言亲切、专业、易懂
- 适合学生理解水平
- 鼓励学生思考和探索茶文化
- 可以适当引用茶文化的经典故事和典故
- 采用启发式、引导式的回答方式，而非直接给出答案

请始终记住：你的主要任务是帮助学生学习和理解茶文化，通过引导和启发的方式，让学生主动思考、探索和总结，培养他们的独立思考能力和对茶文化的深入理解。保持回答的专业性、相关性和教育性。"""
    
    // 构建消息列表，保持最近 n 条消息
    fun makeMessages(
        input: String,
        historyMessages: MutableList<JSONObject>,
        n: Int = 20
    ): JSONArray {
        // 添加用户消息到历史记录
        historyMessages.add(JSONObject().apply {
            put("role", "user")
            put("content", input)
        })
        
        // 如果历史消息超过 n 条，只保留最新的 n 条
        if (historyMessages.size > n) {
            val toRemove = historyMessages.size - n
            repeat(toRemove) {
                historyMessages.removeAt(0)
            }
        }
        
        // 构建新的消息列表
        val messages = JSONArray()
        
        // 首先添加系统消息
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemMessage)
        })
        
        // 然后添加历史消息
        for (i in historyMessages.indices) {
            messages.put(historyMessages[i])
        }
        
        return messages
    }
    
    // 发送聊天请求（非流式）
    fun chat(
        input: String,
        historyMessages: MutableList<JSONObject>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val messages = makeMessages(input, historyMessages)
            
            val requestBody = JSONObject().apply {
                put("model", "kimi-k2-turbo-preview")
                put("messages", messages)
                put("temperature", 0.6)
            }
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBody.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            
            android.util.Log.d("MoonshotApiService", "发送请求到: $baseUrl/chat/completions")
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    android.util.Log.e("MoonshotApiService", "网络请求失败", e)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onError("网络错误: ${e.message ?: "未知错误"}")
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        android.util.Log.d("MoonshotApiService", "收到响应: code=${response.code}")
                        
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            try {
                                if (response.isSuccessful) {
                                    val jsonResponse = JSONObject(responseBody ?: "{}")
                                    val choices = jsonResponse.getJSONArray("choices")
                                    if (choices.length() > 0) {
                                        val message = choices.getJSONObject(0).getJSONObject("message")
                                        val content = message.getString("content")
                                        
                                        // 将助手回复添加到历史记录
                                        historyMessages.add(JSONObject().apply {
                                            put("role", "assistant")
                                            put("content", content)
                                        })
                                        
                                        onSuccess(content)
                                    } else {
                                        onError("API 返回空响应")
                                    }
                                } else {
                                    android.util.Log.e("MoonshotApiService", "API 错误: ${response.code}, $responseBody")
                                    onError("API 错误: ${response.code} - ${responseBody ?: response.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("MoonshotApiService", "解析响应失败", e)
                                onError("解析响应失败: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MoonshotApiService", "读取响应失败", e)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            onError("读取响应失败: ${e.message}")
                        }
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MoonshotApiService", "发送请求异常", e)
            onError("发送请求失败: ${e.message ?: "未知错误"}")
        }
    }

    // 发送聊天请求（流式输出）
    fun chatStream(
        input: String,
        historyMessages: MutableList<JSONObject>,
        onChunk: (String) -> Unit,  // 每收到一个数据块就调用
        onComplete: (String) -> Unit,  // 流结束时调用，返回完整内容
        onError: (String) -> Unit
    ) {
        try {
            val messages = makeMessages(input, historyMessages)

            val requestBody = JSONObject().apply {
                put("model", "kimi-k2-turbo-preview")
                put("messages", messages)
                put("temperature", 0.6)
                put("stream", true)  // 启用流式输出
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            android.util.Log.d("MoonshotApiService", "发送流式请求到: $baseUrl/chat/completions")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    android.util.Log.e("MoonshotApiService", "流式请求失败", e)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onError("网络错误: ${e.message ?: "未知错误"}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        android.util.Log.d("MoonshotApiService", "收到流式响应: code=${response.code}")

                        if (!response.isSuccessful) {
                            val errorBody = response.body?.string()
                            android.util.Log.e("MoonshotApiService", "API 错误: ${response.code}, $errorBody")
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                onError("API 错误: ${response.code} - ${errorBody ?: response.message}")
                            }
                            return
                        }

                        val responseBody = response.body ?: throw IOException("响应体为空")
                        val fullContent = StringBuilder()
                        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

                        // 逐行读取 SSE 响应
                        responseBody.byteStream().bufferedReader().use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                try {
                                    val trimmedLine = line!!.trim()

                                    // SSE 格式: "data: {...}"
                                    if (trimmedLine.startsWith("data: ")) {
                                        val jsonData = trimmedLine.substring(6)  // 移除 "data: " 前缀

                                        // 检查是否是结束标记 [DONE]
                                        if (jsonData == "[DONE]") {
                                            android.util.Log.d("MoonshotApiService", "流式输出完成")
                                            break
                                        }

                                        // 解析 JSON 数据
                                        val jsonObject = JSONObject(jsonData)
                                        val choices = jsonObject.getJSONArray("choices")

                                        if (choices.length() > 0) {
                                            val choice = choices.getJSONObject(0)
                                            val delta = choice.optJSONObject("delta")

                                            if (delta != null && delta.has("content")) {
                                                val content = delta.getString("content")
                                                fullContent.append(content)

                                                // 在主线程回调增量内容
                                                mainHandler.post {
                                                    onChunk(content)
                                                }
                                            }

                                            // 检查是否结束
                                            val finishReason = choice.optString("finish_reason")
                                            if (finishReason == "stop") {
                                                android.util.Log.d("MoonshotApiService", "流式输出正常结束")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MoonshotApiService", "解析数据块失败: $line", e)
                                }
                            }
                        }

                        // 流结束，将完整内容添加到历史记录
                        val completeContent = fullContent.toString()
                        if (completeContent.isNotEmpty()) {
                            historyMessages.add(JSONObject().apply {
                                put("role", "assistant")
                                put("content", completeContent)
                            })
                        }

                        // 在主线程回调完成
                        mainHandler.post {
                            onComplete(completeContent)
                        }

                    } catch (e: Exception) {
                        android.util.Log.e("MoonshotApiService", "处理流式响应失败", e)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            onError("处理响应失败: ${e.message}")
                        }
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("MoonshotApiService", "发送流式请求异常", e)
            onError("发送请求失败: ${e.message ?: "未知错误"}")
        }
    }
}

