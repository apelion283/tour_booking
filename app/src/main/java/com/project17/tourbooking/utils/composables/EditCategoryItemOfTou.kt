package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackWhite0

@Composable
fun EditCategoryOfTourInAdmin(category: Category, onDeleteButtonClick: () -> Unit = {}) {
    Row(
        Modifier
            .border(1.dp, BlackLight300, RoundedCornerShape(16.dp))
            .background(BlackWhite0, RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = category.name, modifier = Modifier.padding(8.dp))
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "",
            modifier = Modifier.clickable { onDeleteButtonClick() })
    }
}