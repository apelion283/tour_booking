package com.project17.tourbooking.activities.user.pay.presentation.booking_success

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.VALUE_TYPE
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Bill
import com.project17.tourbooking.models.CoinPackage
import com.project17.tourbooking.models.TourBooking
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.InformationDefault500
import com.project17.tourbooking.ui.theme.InformationLight400
import com.project17.tourbooking.ui.theme.SuccessDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingSuccessScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    tourBookingBillId: String = "",
    coinBillId: String = "",
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    var tourBookingBill by remember {
        mutableStateOf(TourBooking())
    }
    var email by remember {
        mutableStateOf("")
    }
    var name by remember {
        mutableStateOf("")
    }
    var coinPackageBill by remember {
        mutableStateOf(Bill())
    }
    var coinPackage by remember {
        mutableStateOf(CoinPackage())
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                if(tourBookingBillId.isNotEmpty()){
                    email = authViewModel.currentUserEmail.toString()
                    tourBookingBill = FirestoreHelper.getTourBookingBillByBillId(tourBookingBillId)
                    FirestoreHelper.getCustomerNameByAccountId(tourBookingBill.accountId){
                        if (it != null) {
                            name = it
                        }
                    }
                }
                else if(coinBillId.isNotEmpty()){
                    email = authViewModel.currentUserEmail.toString()
                    coinPackageBill = FirestoreHelper.getCoinPackageBillByBillId(coinBillId)
                    FirestoreHelper.getCustomerNameByAccountId(tourBookingBill.accountId){
                        if (it != null) {
                            name = it
                        }
                    }
                    coinPackage = FirestoreHelper.getCoinPackageByCoinPackageId(coinPackageBill.coinPackageId)!!
                }
            }

            is AuthState.Error -> {
            }

            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationItems.Login.route)
            }

            else -> Unit
        }
    }

    BackHandler {

    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(navController)
            OrderPlacedSuccessSection()
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(BlackLight300)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if(tourBookingBillId.isNotEmpty()){
                TourOrderPlacedInformationSection(
                    personResponsibleName = name,
                    contactInfo = email,
                    tourBookingBill
                )
            }

            if(coinBillId.isNotEmpty()){
                CoinPackageOrderPlacedInformationSection(
                    coinPackageBill = coinPackageBill,
                    coinPackage = coinPackage,
                    email = email,
                    name = name
                )
            }
        }

        FooterSection(
            isBookingTour = tourBookingBillId.isNotEmpty(),
            onBackToHomeClick = { backToHomeAndPopAllInBackStack(navController) },
            onViewYourTripClick = { if(tourBookingBillId.isNotEmpty()) navController.navigate(NavigationItems.MyTrip.route) else {
                navController.navigate(NavigationItems.CoinPackageBooking.route) {
                    popUpTo(NavigationItems.Home.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun HeaderSection(navController: NavController = rememberNavController()){
    Row(Modifier.fillMaxWidth()){
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_home),
            contentDescription = stringResource(id = R.string.image_description_text),
            modifier = Modifier
                .height(50.dp)
                .clickable { backToHomeAndPopAllInBackStack(navController) }
        )
    }
}

@Composable
fun OrderPlacedSuccessSection(){
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.order_placed_successfully),
            contentDescription = stringResource(id = R.string.image_description_text),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.order_placed_successfully_text),
            style = Typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = SuccessDefault500
        )
    }
}

@Composable
fun TourOrderPlacedInformationSection(
    personResponsibleName: String,
    contactInfo: String,
    tourBooking: TourBooking,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.order_placed_information_text),
            style = Typography.headlineMedium,
            color = InformationDefault500,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(16.dp))
        PlaceOrderInformationLine(R.string.person_responsible_name_text, personResponsibleName, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.contact_information_text, contactInfo, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val formattedOrderTime = dateFormat.format(tourBooking.bookingDate.toDate())
        PlaceOrderInformationLine(R.string.placed_order_time_text, formattedOrderTime, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.quantity_of_ticket_text, tourBooking.quantity.toString(), null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.total_text, tourBooking.total.toString(), tourBooking.valueType)
    }
}

@Composable
fun CoinPackageOrderPlacedInformationSection(modifier: Modifier = Modifier, coinPackageBill: Bill, coinPackage: CoinPackage, email: String, name: String) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.order_placed_information_text),
            style = Typography.headlineMedium,
            color = InformationDefault500,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(16.dp))
        PlaceOrderInformationLine(R.string.customer_name_text, name, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.contact_information_text, email, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val formattedOrderTime = dateFormat.format(coinPackageBill.createdDate.toDate())
        PlaceOrderInformationLine(R.string.placed_order_time_text, formattedOrderTime, null)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.quantity_coin_you_got_text, coinPackage.coinValue.toString(), VALUE_TYPE.COIN)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.total_text, coinPackageBill.totalAmount.toString(), VALUE_TYPE.MONEY)
    }
}

@Composable
fun PlaceOrderInformationLine(title: Int, content: String, valueType: VALUE_TYPE?){
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = title),
            style = Typography.titleLarge,
            color = InformationLight400
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            if(valueType != null) {
                if(valueType == VALUE_TYPE.COIN){
                    Image(painter = painterResource(id = R.drawable.coin), contentDescription = "", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                else {
                    Text(
                        text = String.format("$ "),
                        style = Typography.headlineMedium,
                        color = ErrorDefault500
                    )
                }
            }
            
            Text(
                text = content,
                style = Typography.bodyLarge,
                color = BlackDark900
            )
        }
    }
}

fun backToHomeAndPopAllInBackStack(navController: NavController) {
    navController.navigate(NavigationItems.Home.route) {
        popUpTo(NavigationItems.Home.route) {
            inclusive = true
        }
    }
}

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    isBookingTour: Boolean = true,
    onBackToHomeClick: () -> Unit = {},
    onViewYourTripClick: () -> Unit = {},
){
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Button(
            onClick = { onViewYourTripClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = BlackWhite0,
                contentColor = BlackDark900,

            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrandDefault500, RoundedCornerShape(32.dp))
        ){
            Text(
                text = stringResource(id = if(isBookingTour)  R.string.view_your_trip_text else R.string.book_another_package_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onBackToHomeClick() },
            colors = ButtonColors(BrandDefault500 , BlackDark900, BrandDefault500 , BlackDark900),
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = stringResource(id = R.string.back_to_home_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}