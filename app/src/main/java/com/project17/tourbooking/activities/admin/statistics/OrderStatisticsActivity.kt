package com.project17.tourbooking.activities.admin.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.ui.theme.Typography


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderStatisticsScreen() {
    var ordersByUser by remember { mutableStateOf(emptyMap<String, Int>()) }
    var ordersByDate by remember { mutableStateOf(emptyMap<String, Int>()) }
    var ordersByMonth by remember { mutableStateOf(emptyMap<String, Int>()) }
    var topCategories by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(Unit) {
        ordersByUser = FirestoreHelper.getOrdersByUser()
        ordersByDate = FirestoreHelper.getOrdersByDate()
        ordersByMonth = FirestoreHelper.getOrdersByMonth()
        topCategories = FirestoreHelper.getTopSellingCategories(5) // Get top 5 categories
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Thống Kê Đơn Đặt", style = Typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Số lượng đơn đặt theo người dùng:")
        ordersByUser.forEach { (email, count) ->
            Text(text = "$email: $count đơn đặt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Số lượng đơn đặt theo ngày:")
        ordersByDate.forEach { (date, count) ->
            Text(text = "$date: $count đơn đặt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Số lượng đơn đặt theo tháng:")
        ordersByMonth.forEach { (month, count) ->
            Text(text = "$month: $count đơn đặt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Danh mục bán chạy nhất:")
        topCategories.forEach { categoryId ->
            Text(text = "Danh mục ID: $categoryId")
        }
    }
}
