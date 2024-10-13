package com.project17.tourbooking.activities.user.my_trip
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.activities.user.search.presentation.search.SearchBarSection
import com.project17.tourbooking.activities.user.search.viewmodel.SearchViewModel
import com.project17.tourbooking.constant.PAYMENT_STATUS
import com.project17.tourbooking.constant.VALUE_TYPE
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.models.TourBooking
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackDefault500
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.BrandLight300
import com.project17.tourbooking.ui.theme.ErrorLight300
import com.project17.tourbooking.ui.theme.SuccessLight400
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import com.project17.tourbooking.utils.composables.RequireLogin
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MyTripScreen(searchViewModel: SearchViewModel, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var tourBookingList by remember {
        mutableStateOf(emptyList<TourBooking>())
    }

    LaunchedEffect(authState.value, searchViewModel.inputValue.value) {
        tourBookingList = FirestoreHelper.getTourBookingsByAccountId(authViewModel.getCurrentUser()?.uid ?: "")
    }

    if (authState.value is AuthState.Unauthenticated) {
        RequireLogin(navController)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.your_booking_text),
                style = Typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            //SearchBarSection(searchViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tourBookingList) { bill ->
                    TourBookingItem(context, bill, authViewModel)
                }
            }
        }
    }
}

fun formatTimestampToDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

fun isEventPassed(startDate: Timestamp): Boolean {
    val currentDate = Calendar.getInstance().time
    return startDate.toDate().before(currentDate)
}

@Composable
fun TourBookingItem(
    context: Context = LocalContext.current,
    tourBookingBill: TourBooking,
    authViewModel: AuthViewModel
) {
    var isShowReviewDialog by remember { mutableStateOf(false) }
    var review by remember {
        mutableStateOf<Review?>(null)
    }

    var isCancelDialogVisible by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    var isAbleToCancel by remember {
        mutableStateOf(false)
    }
    var status by remember{
        mutableStateOf(tourBookingBill.paymentStatus)
    }

    var tour by remember { mutableStateOf<Tour?>(null) }
    LaunchedEffect(tourBookingBill) {
        tour = FirestoreHelper.getTourByTicketId(tourBookingBill.ticketId)!!
        isAbleToCancel = tourBookingBill.cancellationDeadline.toDate() > Date() && status == PAYMENT_STATUS.SUCCESS && !isEventPassed(tourBookingBill.startTourDate)
        status = tourBookingBill.paymentStatus
        review = FirestoreHelper.getReviewByTourIdAndAccountIdAndTourBookingBillId(tour!!.id, authViewModel.getCurrentUser()?.uid ?: "", tourBookingBill.id)
    }
    if (isShowReviewDialog) {
        AddReviewDialog(
            onDismiss = { isShowReviewDialog = false },
            onSubmit = { rating, comment ->
                if(review == null){
                    review = Review(
                        id = "",
                        rating = rating.toDouble(),
                        comment = comment,
                        createdDate = Timestamp.now(),
                        accountId = authViewModel.getCurrentUser()?.uid ?: "",
                        tourId = tour?.id ?: "",
                        tourBookingBillId = tourBookingBill.id
                    )
                    scope.launch {
                        val result = FirestoreHelper.createReview(review!!)
                        if (result.isSuccess) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.add_review_successfully_text),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.something_went_wrong_text),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                else{
                   scope.launch {
                       val result = FirestoreHelper.updateReviewByReviewId(
                           review!!.id, review!!.copy(
                           rating = rating.toDouble(),
                           comment = comment,
                           createdDate = Timestamp.now()
                       ), review!!
                       )
                       if(result.isSuccess){
                           Toast.makeText(context, context.getString(R.string.update_review_successfully_text), Toast.LENGTH_SHORT).show()
                       }
                       else{
                           Toast.makeText(context, context.getString(R.string.failed_to_update_review_text), Toast.LENGTH_SHORT).show()
                       }
                   }
                }
            },
            review = review
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                //navController.navigate(NavigationItems.TripBookedDetail.route + "/${billIds.first()}")
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlackLight300)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = tour?.image,
                        contentScale = ContentScale.Fit,
                    ), contentDescription = "",
                    Modifier
                        .width(150.dp)
                        .aspectRatio(0.8f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Text(text = tour?.name ?: "", style = Typography.titleLarge)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.total_text) + ":",
                            style = Typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        if (tourBookingBill.valueType == VALUE_TYPE.MONEY) {
                            Text(
                                text = "$${tourBookingBill.total}",
                                style = Typography.bodyMedium,
                                color = BlackDark900
                            )
                        } else if (tourBookingBill.valueType == VALUE_TYPE.COIN) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = "",
                                    Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${tourBookingBill.total}",
                                    style = Typography.bodyMedium,
                                    color = BlackDark900
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val formattedDate = formatTimestampToDate(tourBookingBill.startTourDate)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            style = Typography.bodyMedium,
                            color = BlackLight300
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "",
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tourBookingBill.quantity} ${if (tourBookingBill.quantity > 1) "people" else "person"}",
                            style = Typography.bodyMedium,
                            color = BlackLight400
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                        Text(
                            text = status.toString(),
                            style = Typography.titleLarge,
                            color = if (status == PAYMENT_STATUS.SUCCESS) SuccessLight400 else if (status == PAYMENT_STATUS.CANCELLED) ErrorLight300 else BlackDefault500
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "In: " + formatTimestampToDate(tourBookingBill.bookingDate),
                        style = Typography.bodyMedium,
                        color = BlackLight400,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    { isCancelDialogVisible = true },
                    enabled = isAbleToCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandDefault500,
                        contentColor = BlackDark900
                    )
                ) {
                    Text(text = stringResource(id = R.string.cancel_button_text), Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    enabled = status != PAYMENT_STATUS.CANCELLED && tourBookingBill.startTourDate.toDate() < Date(),
                    onClick = {
                        scope.launch {
                            val result = tour?.let {
                                FirestoreHelper.getReviewByTourIdAndAccountIdAndTourBookingBillId(
                                    it.id, authViewModel.getCurrentUser()?.uid ?: "", tourBookingBill.id)
                            }
                            if(result != null) {
                                review = result
                            }
                            isShowReviewDialog = true
                        }
                              },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandDefault500,
                        contentColor = BlackDark900
                    )
                ) {
                    Text(text = "Review", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }

                CommonAlertDialog(
                    isDialogVisible = isCancelDialogVisible ,
                    onDismiss = { isCancelDialogVisible = false },
                    onConfirm = {
                        scope.launch {
                            val result = FirestoreHelper.cancelTourBookingBill(tourBookingBill.id)
                            if(result.isSuccess){
                                status = PAYMENT_STATUS.CANCELLED
                                isAbleToCancel = false
                            }
                            isCancelDialogVisible = false
                        }
                    },
                    title = R.string.cancel_booking_text,
                    message = R.string.are_your_sure_text,
                    confirmButtonText = R.string.confirm_button_text,
                    dismissButtonText = R.string.cancel_button_text
                )
            }
        }
    }
}


@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Float, String) -> Unit,
    review: Review?,
) {
    var rating by remember { mutableIntStateOf(review?.rating?.toInt() ?: 0) }
    var comment by remember {
        mutableStateOf(review?.comment ?: "")
    }
    AlertDialog(
        containerColor = BlackWhite0,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_your_review_text)) },
        text = {
            Column {
                Text(stringResource(id = R.string.rating_text) + ":", style = Typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(BlackWhite0, RoundedCornerShape(16.dp))
                        .border(1.dp, BrandLight300, RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) {index ->
                        Image(
                            painter = painterResource(id = if(index + 1 <= rating) R.drawable.ic_yellow_star_3x else R.drawable.ic_white_star_3x),
                            contentDescription = "",
                            Modifier
                                .size(40.dp)
                                .clickable(onClick = {
                                    rating = index + 1
                                })
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(id = R.string.your_comment_text), style = Typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = {comment = it},
                    placeholder = { Text(stringResource(id = R.string.enter_your_comment_text)) },
                    colors = TextFieldDefaults.colors(
                        cursorColor = BrandDefault500,
                        focusedContainerColor = BlackWhite0,
                        unfocusedContainerColor = BlackWhite0,
                        focusedIndicatorColor = BrandDefault500,
                        unfocusedIndicatorColor = BlackLight300,
                    ),
                    modifier = Modifier.heightIn(min = 150.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(rating.toFloat(), comment)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDefault500,
                    contentColor = BlackDark900
                )
            ) {
                Text(
                    stringResource(id = R.string.submit_button_text),
                    Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDefault500,
                    contentColor = BlackDark900
                )
            ) {
                Text(
                    stringResource(id = R.string.cancel_button_text),
                    Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    )
}

@Preview
@Composable
fun MyTripScreenPreview() {
    TourBookingTheme {
        MyTripScreen(searchViewModel = viewModel(), navController = rememberNavController(), authViewModel = viewModel())
    }
}