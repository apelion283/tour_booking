package com.project17.tourbooking.utils.modifiers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.ui.theme.BlackWhite0

fun Modifier.iconWithBackgroundModifier(isBackground: Boolean = true): Modifier {
    return this
        .padding(16.dp)
        .clip(CircleShape)
        .background(if (isBackground) BlackWhite0 else Color.Transparent)
        .padding(8.dp)
}