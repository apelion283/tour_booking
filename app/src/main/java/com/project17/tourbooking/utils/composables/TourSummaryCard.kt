package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.Typography

@Composable
fun TourSummaryCard(tour: Tour, location: String) {

    Box(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(tour.image),
            contentDescription = stringResource(id = R.string.image_description_text),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                Spacer(modifier = Modifier.weight(1f))

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = tour.name,
                        style = Typography.headlineMedium,
                        color = BlackWhite0
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = location,
                            style = Typography.titleLarge,
                            color = BlackWhite0
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.people_have_explored_text, tour.bookingCount),
                        style = Typography.bodyLarge,
                        color = BlackWhite0
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GenerateStarFromRating(rating = tour.averageRating, textColor = BlackWhite0)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

