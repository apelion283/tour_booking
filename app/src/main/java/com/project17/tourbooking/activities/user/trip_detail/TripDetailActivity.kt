package com.project17.tourbooking.activities.user.trip_detail

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.MAX_REVIEW_LIMIT
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackDefault500
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.AddToWishList
import com.project17.tourbooking.utils.composables.CategoryItem
import com.project17.tourbooking.utils.composables.ReviewItem
import com.project17.tourbooking.utils.composables.TourSummaryCard
import com.project17.tourbooking.utils.modifiers.iconWithBackgroundModifier
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripDetailScreen(navController: NavController = rememberNavController(), tourId: String, location: String) {
    var tour by remember { mutableStateOf(Tour()) }
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(tourId) {
        if (tourId.isNotEmpty()) {
            tour = FirestoreHelper.getTourByTourId(tourId)!!
        }
    }
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            NavBarSection(tour, navController)
            Spacer(modifier = Modifier.height(16.dp))
            TourSummaryCard(tour = tour, location = location )
            Spacer(modifier = Modifier.height(32.dp))
            CategoryListSection(tourId = tour.id)
            Spacer(modifier = Modifier.height(32.dp))
            AboutTripSection(tour = tour)
            Spacer(modifier = Modifier.height(32.dp))
            ReviewSection(tourId = tour.id)
        }
        FooterSection(
            tour = tour,
            modifier = Modifier.align(Alignment.BottomStart),
            onBookingButtonClick = { navController.navigate(NavigationItems.BookingDetail.route + "/${tourId}") },
            navController = navController,
            authViewModel = authViewModel
        )
    }
}


@Composable
fun CategoryListSection(tourId: String) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    if (tourId.isNotEmpty()) {
        LaunchedEffect(tourId) {
            categories = FirestoreHelper.getCategoriesOfTourByTourId(tourId)
        }
    }
    Column {
        Text(
            text = stringResource(R.string.category_text),
            style = Typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow {
            items(categories) { category ->
                CategoryItem(category = category, onClick = {})
            }
        }
    }
}

@Composable
fun AboutTripSection(tour: Tour) {
    Column {
        Text(
            text = stringResource(R.string.about_trip_text),
            style = Typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = tour.description,
            style = Typography.bodyLarge,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewSection(tourId: String) {
    var reviews by remember {
        mutableStateOf<List<Review>>(emptyList())
    }
    var averageRating by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(tourId) {
        if (tourId.isNotEmpty()) {
            reviews = FirestoreHelper.getReviewsByTourId(tourId)
            averageRating = FirestoreHelper.getAverageRatingByTourId(tourId)
        }
    }

    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (reviews.size < MAX_REVIEW_LIMIT)
                    stringResource(R.string.review_text, reviews.size)
                else stringResource(R.string.review_plus_text, MAX_REVIEW_LIMIT),
                style = Typography.headlineMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.ic_yellow_star_3x),
                contentDescription = stringResource(id = R.string.image_description_text),
                modifier = Modifier
                    .size(30.dp)
            )
            Text(
                text = String.format("%.1f", averageRating),
                style = Typography.headlineSmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if(reviews.isNotEmpty()){
            LazyColumn(Modifier.height(if (reviews.isEmpty()) 0.dp else 300.dp)) {
                items(reviews) { review ->
                    ReviewItem(review = review)
                }
            }
        }
        else {
            Text(
                text = stringResource(id = R.string.there_is_no_reviews_text),
                style = Typography.bodyLarge,
                color = BlackDefault500
                )
        }
    }
}


@Composable
fun NavBarSection(
    tour: Tour,
    navController: NavController
) {
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            tint = BlackDark900,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { navController.popBackStack() }
        )
        Text(
            text = tour.name,
            style = Typography.titleLarge,
            color = BlackDark900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        AddToWishList(
            initiallyAddedToWishList = false,
            modifier = Modifier
                .iconWithBackgroundModifier()
                .align(Alignment.Top)
        )
    }

}


@Composable
fun FooterSection(
    tour: Tour,
    modifier: Modifier,
    onBookingButtonClick: () -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    val isAuthenticated = authState is AuthState.Authenticated
    var ticket by remember { mutableStateOf<Ticket?>(null) }

    LaunchedEffect(tour) {
        ticket = FirestoreHelper.getTicketOfTourByTourId(tour.id)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlackWhite0),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically){
                Column {
                    Row {
                        Text(
                            text = String.format("$" + "%.2f", ticket?.moneyPrice?.toDouble() ?: 0.0),
                            style = Typography.headlineMedium,
                            color = ErrorDefault500
                        )
                    }
                    Row {
                        Text(
                            text = "or",
                            style = Typography.titleSmall,
                            color = BlackLight300
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = stringResource(
                                    id = R.string.image_description_text
                                ),
                                modifier = Modifier
                                    .size(20.dp)
                            )

                            Text(
                                text = String.format("%d", ticket?.coinPrice ?: 0),
                                style = Typography.headlineMedium,
                                color = ErrorDefault500
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = stringResource(id = R.string.person_text),
                    color = BlackLight400,
                    style = Typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isAuthenticated) {
                        onBookingButtonClick()
                    } else {
                        navController.navigate(NavigationItems.Login.route)
                    }
                },
                enabled = tour.closeRegistrationDate > Timestamp.now(),
                colors = ButtonColors(BrandDefault500 , BlackDark900, BlackLight300 , BlackDark900),
                modifier = Modifier
                    .width(150.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.booking_button_text),
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
