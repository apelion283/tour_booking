package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackLight200
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.ErrorDark600
import com.project17.tourbooking.ui.theme.Typography

@Composable
fun TourCardInHorizontal(
    modifier: Modifier = Modifier,
    tour: Tour,
    navController: NavHostController,
    onMeasured: (Dp) -> Unit = {}
) {
    val density = LocalDensity.current
    val tourId = tour.id
    var tourPrice by remember{
        mutableStateOf(Pair<Long, Long>(0, 0))
    }

    var destination by remember {
        mutableStateOf(Destination())
    }

    LaunchedEffect(Unit) {
        tourPrice = FirestoreHelper.getTicketPriceByTourId(tourId)
        destination = FirestoreHelper.getDestinationById(tour.destinationId)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val height = with(density) { coordinates.size.height.toDp() }
                onMeasured(height)
            }
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = {
                navController.navigate(NavigationItems.TripDetail.route + "/${tourId}/${destination.location}")
            })
            .border(
                width = 1.dp,
                color = BlackLight200,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackWhite0)
                .padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(tour.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxHeight()
                    .weight(2f)
                    .aspectRatio(0.8f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlackWhite0)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tour.name,
                        style = Typography.titleLarge,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("$" + "%.2f", tourPrice.first.toDouble()),
                            style = Typography.bodyLarge,
                            color = ErrorDark600
                        )
                        Text(
                            text = "/ ",
                            style = Typography.bodyLarge,
                            color = ErrorDark600
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = stringResource(
                                    id = R.string.image_description_text
                                ),
                                modifier = Modifier
                                    .size(15.dp)
                            )

                            Text(
                                text = String.format("%d", tourPrice.second),
                                style = Typography.bodyLarge,
                                color = ErrorDark600
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    GenerateStarFromRating(rating = tour.averageRating, Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = tour.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = BlackLight400,
                        style = Typography.bodyMedium,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

