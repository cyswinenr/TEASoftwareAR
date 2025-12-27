package Teacourse.apk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Teacourse.apk.navigation.Screen

data class TaskItem(
    val title: String,
    val route: String,
    val color: Color
)

@Composable
fun TaskOverviewScreen(
    onTaskClick: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    // 调整顺序，让思考题一在第二行（3列布局）
    // 第一行：任务一、任务二、（空）
    // 第二行：思考题一、思考题二、（空）
    // 第三行：创意题、（空）、（空）
    val tasks = listOf(
        // 任务一、二：绿色系（茶文化主题）
        TaskItem("任务一：泡茶体验、品茶时", Screen.Task1.route, Color(0xFF4CAF50)),
        TaskItem("任务二：泡出你心中的那杯茶", Screen.Task2.route, Color(0xFF4CAF50)),
        // 占位项（透明，不显示）
        TaskItem("", "", Color.Transparent),
        // 思考题一、二：橙色系（代表思考、智慧）- 放在第二行
        TaskItem("思考题一", Screen.Thinking1.route, Color(0xFFFF9800)),
        TaskItem("思考题二", Screen.Thinking2.route, Color(0xFFFF9800)),
        // 占位项（透明，不显示）
        TaskItem("", "", Color.Transparent),
        // 创意题：紫色系（代表创意、想象力）
        TaskItem("创意题", Screen.Creative.route, Color(0xFF9C27B0))
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5DC),
                        Color(0xFFE8F5E9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            // 标题和返回按钮在同一行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 标题
                Text(
                    text = "任务总览",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                
                // 按钮组（右上角）
                Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 查看汇总按钮
                    Button(
                        onClick = onSummaryClick,
                        modifier = Modifier
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "查看汇总",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // 返回按钮
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
            }
            
            // 任务网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    if (task.route.isNotEmpty()) {
                        TaskCard(
                            task = task,
                            onClick = { onTaskClick(task.route) }
                        )
                    } else {
                        // 占位项，不显示
                        Spacer(modifier = Modifier.fillMaxWidth().height(200.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = task.color
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = task.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }
    }
}

