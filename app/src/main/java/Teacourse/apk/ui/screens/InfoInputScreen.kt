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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoInputScreen(
    onNextClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("TeaCultureApp", Context.MODE_PRIVATE)
    }
    
    // 从 SharedPreferences 加载已保存的数据
    var school by remember { 
        mutableStateOf(sharedPreferences.getString("school", "") ?: "")
    }
    var grade by remember { 
        mutableStateOf(sharedPreferences.getString("grade", "") ?: "")
    }
    var classNumber by remember { 
        mutableStateOf(sharedPreferences.getString("classNumber", "") ?: "")
    }
    var date by remember { 
        mutableStateOf(sharedPreferences.getString("date", getCurrentDate()) ?: getCurrentDate())
    }
    var selectedMemberCount by remember { 
        mutableStateOf(sharedPreferences.getInt("memberCount", 0))
    }
    var memberNames by remember { 
        mutableStateOf(
            List(10) { index ->
                sharedPreferences.getString("memberName_$index", "") ?: ""
            }
        )
    }
    var groupNumber by remember { 
        mutableStateOf(sharedPreferences.getInt("groupNumber", 0))
    }
    var gradeExpanded by remember { mutableStateOf(false) }
    var memberCountExpanded by remember { mutableStateOf(false) }
    var groupNumberExpanded by remember { mutableStateOf(false) }
    
    val gradeOptions = listOf("高一", "高二")
    val memberCountOptions = (1..10).toList()
    val groupNumberOptions = (1..12).toList()
    
    val scrollState = rememberScrollState()
    
    // 保存数据的函数
    fun saveData() {
        with(sharedPreferences.edit()) {
            putString("school", school)
            putString("grade", grade)
            putString("classNumber", classNumber)
            putString("date", date)
            putInt("memberCount", selectedMemberCount)
            putInt("groupNumber", groupNumber)
            memberNames.forEachIndexed { index, name ->
                putString("memberName_$index", name)
            }
            apply()
        }
        Toast.makeText(context, "信息保存成功！", Toast.LENGTH_SHORT).show()
    }
    
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
            
            // 年级、班和小组成员人数在同一行
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 年级选择（固定宽度，更美观）
                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = !gradeExpanded },
                    modifier = Modifier.weight(0.8f)
                ) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { },
                        readOnly = true,
                        label = { 
                            Text(
                                "年级", 
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF81C784),
                            focusedContainerColor = Color(0xFFF1F8F4),
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color(0xFF757575)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = if (grade.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (grade.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (gradeExpanded) Color(0xFF4CAF50) else Color(0xFF81C784),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false },
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0x40000000)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        gradeOptions.forEachIndexed { index, option ->
                            val isSelected = grade == option
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 2.dp else 0.dp
                                )
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = option,
                                                fontSize = 20.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF424242),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "已选择",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        grade = option
                                        gradeExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.Transparent,
                                        leadingIconColor = Color.Transparent,
                                        trailingIconColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                // 班级输入（只能输入数字）
                OutlinedTextField(
                    value = classNumber,
                    onValueChange = { newValue ->
                        // 只允许数字
                        if (newValue.all { it.isDigit() }) {
                            classNumber = newValue
                        }
                    },
                    label = { Text("班", fontSize = 18.sp) },
                    modifier = Modifier
                        .weight(0.6f)
                        .height(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF81C784)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("请输入班级号", fontSize = 16.sp) }
                )
                
                // 小组成员人数选择
                ExposedDropdownMenuBox(
                    expanded = memberCountExpanded,
                    onExpandedChange = { memberCountExpanded = !memberCountExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (selectedMemberCount == 0) "" else selectedMemberCount.toString(),
                        onValueChange = { },
                        readOnly = true,
                        label = { 
                            Text(
                                "小组成员人数", 
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            unfocusedBorderColor = Color(0xFF81C784),
                            focusedContainerColor = Color(0xFFF1F8F4),
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color(0xFF757575)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = if (selectedMemberCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedMemberCount > 0) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (memberCountExpanded) Color(0xFF4CAF50) else Color(0xFF81C784),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = memberCountExpanded,
                        onDismissRequest = { memberCountExpanded = false },
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Color(0x40000000)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        memberCountOptions.forEachIndexed { index, count ->
                            val isSelected = selectedMemberCount == count
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 2.dp else 0.dp
                                )
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "$count 人",
                                                fontSize = 20.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF424242),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "已选择",
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedMemberCount = count
                                        memberCountExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.Transparent,
                                        leadingIconColor = Color.Transparent,
                                        trailingIconColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            // 小组编号选择
            ExposedDropdownMenuBox(
                expanded = groupNumberExpanded,
                onExpandedChange = { groupNumberExpanded = !groupNumberExpanded },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                OutlinedTextField(
                    value = if (groupNumber == 0) "" else groupNumber.toString(),
                    onValueChange = { },
                    readOnly = true,
                    label = { 
                        Text(
                            "小组编号", 
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color(0xFF81C784),
                        focusedContainerColor = Color(0xFFF1F8F4),
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = Color(0xFF2E7D32),
                        unfocusedLabelColor = Color(0xFF757575)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = if (groupNumber > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (groupNumber > 0) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (groupNumberExpanded) Color(0xFF4CAF50) else Color(0xFF81C784),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
                
                ExposedDropdownMenu(
                    expanded = groupNumberExpanded,
                    onDismissRequest = { groupNumberExpanded = false },
                    modifier = Modifier
                        .widthIn(max = 250.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Color(0x40000000)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    groupNumberOptions.forEachIndexed { index, number ->
                        val isSelected = groupNumber == number
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 2.dp else 0.dp
                            )
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "小组 $number",
                                            fontSize = 20.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF424242),
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "已选择",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    groupNumber = number
                                    groupNumberExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.Transparent,
                                    leadingIconColor = Color.Transparent,
                                    trailingIconColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
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
                        saveData()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                
                // 下一页按钮
                Button(
                    onClick = {
                        saveData()
                        onNextClick()
                    },
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

