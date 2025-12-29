package Teacourse.apk.utils

import android.content.Context
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class DataSubmissionService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // 将照片转换为 Base64
    private fun encodePhotoToBase64(photoPath: String): String? {
        return try {
            val file = File(photoPath)
            if (file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // 收集所有数据并转换为 JSON
    fun collectAllDataAsJson(): JSONObject? {
        return try {
            val studentPrefs = context.getSharedPreferences("TeaCultureApp", Context.MODE_PRIVATE)
            val task1Prefs = context.getSharedPreferences("Task1Data", Context.MODE_PRIVATE)
            val task2Prefs = context.getSharedPreferences("Task2Data", Context.MODE_PRIVATE)
            val thinking1Prefs = context.getSharedPreferences("Thinking1Data", Context.MODE_PRIVATE)
            val thinking2Prefs = context.getSharedPreferences("Thinking2Data", Context.MODE_PRIVATE)
            val creativePrefs = context.getSharedPreferences("CreativeData", Context.MODE_PRIVATE)
            
            val json = JSONObject()
            
            // 学生信息
            val studentInfo = JSONObject().apply {
                put("school", studentPrefs.getString("school", "") ?: "")
                put("grade", studentPrefs.getString("grade", "") ?: "")
                put("classNumber", studentPrefs.getString("classNumber", "") ?: "")
                put("date", studentPrefs.getString("date", "") ?: "")
                put("memberCount", studentPrefs.getInt("memberCount", 0))
                put("groupNumber", studentPrefs.getInt("groupNumber", 0))
                val memberNames = JSONArray()
                for (i in 0 until 10) {
                    val name = studentPrefs.getString("memberName_$i", "") ?: ""
                    if (name.isNotEmpty()) {
                        memberNames.put(name)
                    }
                }
                put("memberNames", memberNames)
            }
            json.put("studentInfo", studentInfo)
            
            // 任务一数据
            val task1Photos = JSONArray()
            try {
                (task1Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).forEach { path ->
                    try {
                        encodePhotoToBase64(path)?.let { task1Photos.put(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "处理任务一照片失败: $path", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSubmissionService", "读取任务一照片路径失败", e)
            }
            
            val task1 = JSONObject().apply {
            put("teaName", task1Prefs.getString("teaName", "") ?: "")
            put("teacherTeaName", task1Prefs.getString("teacherTeaName", "") ?: "")
            put("teaCategory", task1Prefs.getString("teaCategory", "") ?: "")
            put("waterTemperature", task1Prefs.getString("waterTemperature", "") ?: "")
            put("brewingDuration", task1Prefs.getString("brewingDuration", "") ?: "")
            
            // 干茶
            val dryTea = JSONObject().apply {
                put("color", task1Prefs.getString("dryTeaColor", "") ?: "")
                put("aroma", task1Prefs.getString("dryTeaAroma", "") ?: "")
                put("shape", task1Prefs.getString("dryTeaShape", "") ?: "")
                put("taste", task1Prefs.getString("dryTeaTaste", "") ?: "")
            }
            put("dryTea", dryTea)
            
            // 茶汤
            val teaLiquor = JSONObject().apply {
                put("color", task1Prefs.getString("teaLiquorColor", "") ?: "")
                put("aroma", task1Prefs.getString("teaLiquorAroma", "") ?: "")
                put("shape", task1Prefs.getString("teaLiquorShape", "") ?: "")
                put("taste", task1Prefs.getString("teaLiquorTaste", "") ?: "")
            }
            put("teaLiquor", teaLiquor)
            
            // 叶底
            val spentLeaves = JSONObject().apply {
                put("color", task1Prefs.getString("spentLeavesColor", "") ?: "")
                put("aroma", task1Prefs.getString("spentLeavesAroma", "") ?: "")
                put("shape", task1Prefs.getString("spentLeavesShape", "") ?: "")
                put("taste", task1Prefs.getString("spentLeavesTaste", "") ?: "")
            }
            put("spentLeaves", spentLeaves)
            
                put("reflectionAnswer", task1Prefs.getString("reflectionAnswer", "") ?: "")
                put("photos", task1Photos)
            }
            json.put("task1", task1)
            
            // 任务二数据
            val task2Photos = JSONArray()
            try {
                (task2Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).forEach { path ->
                    try {
                        encodePhotoToBase64(path)?.let { task2Photos.put(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "处理任务二照片失败: $path", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSubmissionService", "读取任务二照片路径失败", e)
            }
            
            val task2 = JSONObject().apply {
            put("teaName", task2Prefs.getString("teaName", "") ?: "")
            put("waterTemperature", task2Prefs.getString("waterTemperature", "") ?: "")
            put("steepingDuration", task2Prefs.getString("steepingDuration", "") ?: "")
            put("teaColor", task2Prefs.getString("teaColor", "") ?: "")
            put("teaAroma", task2Prefs.getString("teaAroma", "") ?: "")
            put("teaTaste", task2Prefs.getString("teaTaste", "") ?: "")
            put("meetsExpectation", task2Prefs.getBoolean("meetsExpectation", false))
            put("notMeetsExpectation", task2Prefs.getBoolean("notMeetsExpectation", false))
                put("reflectionAnswer", task2Prefs.getString("reflectionAnswer", "") ?: "")
                put("photos", task2Photos)
            }
            json.put("task2", task2)
            
            // 思考题一
            val thinking1Photos = JSONArray()
            try {
                (thinking1Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).forEach { path ->
                    try {
                        encodePhotoToBase64(path)?.let { thinking1Photos.put(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "处理思考题一照片失败: $path", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSubmissionService", "读取思考题一照片路径失败", e)
            }
            
            val thinking1 = JSONObject().apply {
                put("answer", thinking1Prefs.getString("answer", "") ?: "")
                put("photos", thinking1Photos)
            }
            json.put("thinking1", thinking1)
            
            // 思考题二
            val thinking2Photos = JSONArray()
            try {
                (thinking2Prefs.getStringSet("photoPaths", setOf()) ?: setOf()).forEach { path ->
                    try {
                        encodePhotoToBase64(path)?.let { thinking2Photos.put(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "处理思考题二照片失败: $path", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSubmissionService", "读取思考题二照片路径失败", e)
            }
            
            val thinking2 = JSONObject().apply {
                put("answer", thinking2Prefs.getString("answer", "") ?: "")
                put("photos", thinking2Photos)
            }
            json.put("thinking2", thinking2)
            
            // 创意题
            val creativePhotos = JSONArray()
            try {
                (creativePrefs.getStringSet("photoPaths", setOf()) ?: setOf()).forEach { path ->
                    try {
                        encodePhotoToBase64(path)?.let { creativePhotos.put(it) }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "处理创意题照片失败: $path", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DataSubmissionService", "读取创意题照片路径失败", e)
            }
            
            val creative = JSONObject().apply {
                put("answer", creativePrefs.getString("answer", "") ?: "")
                put("photos", creativePhotos)
            }
            json.put("creative", creative)
            
            // 添加提交时间戳
            json.put("submitTime", System.currentTimeMillis())
            
            json
        } catch (e: Exception) {
            android.util.Log.e("DataSubmissionService", "收集数据失败", e)
            e.printStackTrace()
            null
        }
    }
    
    // 提交数据到服务器
    fun submitData(
        serverUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            android.util.Log.d("DataSubmissionService", "开始提交数据，服务器地址: $serverUrl")
            
            // 验证URL格式
            if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                android.util.Log.e("DataSubmissionService", "URL格式错误: $serverUrl")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError("服务器地址格式不正确，应以http://或https://开头")
                }
                return
            }
            
            // 收集数据
            android.util.Log.d("DataSubmissionService", "开始收集数据...")
            val jsonData = collectAllDataAsJson()
            if (jsonData == null) {
                android.util.Log.e("DataSubmissionService", "收集数据失败")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError("收集数据失败，请检查数据完整性")
                }
                return
            }
            
            val jsonString = jsonData.toString()
            android.util.Log.d("DataSubmissionService", "数据收集完成，大小: ${jsonString.length} 字符")
            
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonString.toRequestBody(mediaType)
            
            val fullUrl = "$serverUrl/api/submit"
            android.util.Log.d("DataSubmissionService", "构建请求，URL: $fullUrl")
            
            val request = Request.Builder()
                .url(fullUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()
            
            android.util.Log.d("DataSubmissionService", "发送请求...")
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    android.util.Log.e("DataSubmissionService", "网络请求失败", e)
                    e.printStackTrace()
                    // 使用Handler切换到主线程
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        onError("网络错误: ${e.message ?: "未知错误"}")
                    }
                }
                
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        android.util.Log.d("DataSubmissionService", "收到响应: code=${response.code}, body=$responseBody")
                        
                        // 使用Handler切换到主线程
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            try {
                                if (response.isSuccessful) {
                                    android.util.Log.d("DataSubmissionService", "提交成功")
                                    onSuccess(responseBody ?: "提交成功")
                                } else {
                                    android.util.Log.e("DataSubmissionService", "服务器错误: ${response.code}")
                                    onError("服务器错误: ${response.code} - ${responseBody ?: response.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("DataSubmissionService", "处理响应回调失败", e)
                                onError("处理响应失败: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DataSubmissionService", "读取响应失败", e)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            onError("读取响应失败: ${e.message}")
                        }
                    } finally {
                        response.close()
                    }
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("DataSubmissionService", "提交数据异常", e)
            e.printStackTrace()
            onError("提交失败: ${e.message ?: "未知错误"}")
        }
    }
}

