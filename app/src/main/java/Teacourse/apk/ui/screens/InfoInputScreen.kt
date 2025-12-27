package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoInputScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    var school by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(getCurrentDate()) }
    var selectedMemberCount by remember { mutableStateOf(0) }
    var memberNames by remember { mutableStateOf(List(10) { "" }) }
    var expanded by remember { mutableStateOf(false) }
    
    val memberCountOptions = (1..10).toList()
    
    val scrollState = rememberScrollState()
    
    // 当人数改变时，重置超出范围的姓名
    LaunchedEffect(selectedMemberCount) {
        if (selectedMemberCount > 0) {
            // 保持前selectedMemberCount个姓名的值，其余清空
            val newNames = memberNames.toMutableList()
            for (i in selectedMemberCount until 10) {
                newNames[i] = ""
            }
            memberNames = newNames
        }
    }
    
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
            // 返回按钮和标题行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81C784)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "返回",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
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
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                singleLine = true
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
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                singleLine = true
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
                singleLine = true,
                enabled = false
            )
            
            // 小组成员人数选择
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                OutlinedTextField(
                    value = if (selectedMemberCount == 0) "" else selectedMemberCount.toString(),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("小组成员人数", fontSize = 18.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF81C784)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    memberCountOptions.forEach { count ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$count 人",
                                    fontSize = 20.sp
                                )
                            },
                            onClick = {
                                selectedMemberCount = count
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // 动态显示小组成员姓名输入框
            if (selectedMemberCount > 0) {
                Text(
                    text = "小组成员姓名",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(top = 10.dp, bottom = 10.dp)
                )
                
                // 使用网格布局显示姓名输入框（每行2个）
                for (i in 0 until selectedMemberCount step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 第一个输入框
                        OutlinedTextField(
                            value = memberNames[i],
                            onValueChange = { newValue ->
                                // 限制最多5个中文字符
                                val chineseCharCount = newValue.count { it in '\u4e00'..'\u9fa5' }
                                if (chineseCharCount <= 5) {
                                    val newNames = memberNames.toMutableList()
                                    newNames[i] = newValue
                                    memberNames = newNames
                                }
                            },
                            label = { Text("成员${i + 1}", fontSize = 18.sp) },
                            placeholder = { Text("请输入姓名（最多5个汉字）", fontSize = 16.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF81C784)
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                            singleLine = true
                        )
                        
                        // 第二个输入框（如果存在）
                        if (i + 1 < selectedMemberCount) {
                            OutlinedTextField(
                                value = memberNames[i + 1],
                                onValueChange = { newValue ->
                                    // 限制最多5个中文字符
                                    val chineseCharCount = newValue.count { it in '\u4e00'..'\u9fa5' }
                                    if (chineseCharCount <= 5) {
                                        val newNames = memberNames.toMutableList()
                                        newNames[i + 1] = newValue
                                        memberNames = newNames
                                    }
                                },
                                label = { Text("成员${i + 2}", fontSize = 18.sp) },
                                placeholder = { Text("请输入姓名（最多5个汉字）", fontSize = 16.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    unfocusedBorderColor = Color(0xFF81C784)
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                                singleLine = true
                            )
                        } else {
                            // 占位，保持布局平衡
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
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

