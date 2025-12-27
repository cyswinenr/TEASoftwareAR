package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InfoInputScreen(onNextClick: () -> Unit) {
    var school by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(getCurrentDate()) }
    var memberCount by remember { mutableStateOf("") }
    var memberNames by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            // 标题
            Text(
                text = "信息录入",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // 学校输入
            OutlinedTextField(
                value = school,
                onValueChange = { school = it },
                label = { Text("学校", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
            )
            
            // 班级输入
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("班级", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
            )
            
            // 日期输入
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("日期", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                enabled = false
            )
            
            // 小组成员人数输入
            OutlinedTextField(
                value = memberCount,
                onValueChange = { memberCount = it },
                label = { Text("小组成员人数", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(70.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
            )
            
            // 小组成员姓名输入
            OutlinedTextField(
                value = memberNames,
                onValueChange = { memberNames = it },
                label = { Text("小组成员姓名（用逗号分隔）", fontSize = 18.sp) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF81C784)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 按钮行
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                // 保存按钮
                Button(
                    onClick = {
                        // TODO: 保存数据到本地存储
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81C784)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                
                // 下一页按钮
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("下一页", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

