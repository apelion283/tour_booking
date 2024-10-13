package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.modifiers.iconWithBackgroundModifier

@Composable
fun TourCardInVertical(
    tour: Tour,
    navController: NavHostController
) {

    var destination by remember {
        mutableStateOf(Destination())
    }

    LaunchedEffect(Unit) {
        destination = FirestoreHelper.getDestinationById(tour.destinationId)
    }

    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = {
                navController.navigate(NavigationItems.TripDetail.route + "/${tour.id}/${destination.location}")
            })
    ) {
        AsyncImage(
            model = tour.image,
            contentDescription = "Tour Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxHeight()
        )
        AddToWishList(
            initiallyAddedToWishList = false,
            modifier = Modifier
                .iconWithBackgroundModifier()
                .align(Alignment.TopEnd)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = tour.name,
                style = Typography.titleLarge,
                color = BlackWhite0
            )
            Spacer(modifier = Modifier.height(4.dp))
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
                    text = destination.location,
                    style = Typography.bodyMedium,
                    color = BlackWhite0
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            GenerateStarFromRating(rating = tour.averageRating)
        }
    }
    Spacer(modifier = Modifier.width(16.dp))
}