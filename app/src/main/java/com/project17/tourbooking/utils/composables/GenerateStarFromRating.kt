package com.project17.tourbooking.utils.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.R
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.Typography

@SuppressLint("DefaultLocale")
@Composable
fun GenerateStarFromRating(
    rating: Double,
    textColor: Color = BlackWhite0,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val starSize = 16.dp
        if (rating.toInt() == 5) {
            repeat(5) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yellow_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        } else {
            val filledStars = rating.toInt()
            val emptyStars = 5 - filledStars
            repeat(filledStars) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yellow_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            repeat(emptyStars) {
                Image(
                    painter = painterResource(id = R.drawable.ic_white_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Text(
            text = String.format("%.2f", rating),
            style = Typography.titleMedium,
            color = textColor
        )
    }
}
