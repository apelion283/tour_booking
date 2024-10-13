package com.project17.tourbooking.utils.composables

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.ui.theme.Typography
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewItem(review: Review){
    var name by remember { mutableStateOf("Loading...") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirestoreHelper.getAvatarUrlFromAccountId(review.accountId) { url ->
            avatarUrl = url
        }
        FirestoreHelper.getCustomerNameByAccountId(review.accountId){
            name = it ?: "null"
        }
    }
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = avatarUrl).apply(block = fun ImageRequest.Builder.() {
                    placeholder(R.drawable.avatar_placeholder)
                    error(R.drawable.avatar_placeholder)
                }).build()
            ),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = Typography.titleLarge,
                )
                Text(
                    text = calculateReviewDateDisplay(review.createdDate),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            GenerateStarFromRating(rating = review.rating)

            Text(
                text = review.comment,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun calculateReviewDateDisplay(reviewDate: Timestamp): String {
    val reviewCalendar = Calendar.getInstance().apply {
        time = reviewDate.toDate()
    }
    val currentCalendar = Calendar.getInstance()

    val years = currentCalendar.get(Calendar.YEAR) - reviewCalendar.get(Calendar.YEAR)
    val months = currentCalendar.get(Calendar.MONTH) - reviewCalendar.get(Calendar.MONTH)
    val days = currentCalendar.get(Calendar.DAY_OF_MONTH) - reviewCalendar.get(Calendar.DAY_OF_MONTH)

    return when {
        years > 0 -> {
            val adjustedMonths = if (months < 0) months + 12 else months
            if (adjustedMonths > 0) {
                "$years years $adjustedMonths months ago"
            } else {
                "$years years ago"
            }
        }
        months > 0 -> "$months months ago"
        days > 0 -> "$days days ago"
        else -> "Today"
    }
}