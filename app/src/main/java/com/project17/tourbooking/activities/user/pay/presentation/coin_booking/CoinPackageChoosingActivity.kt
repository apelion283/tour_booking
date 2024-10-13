package com.project17.tourbooking.activities.user.pay.presentation.coin_booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.activities.user.pay.viewmodel.PayViewModel
import com.project17.tourbooking.activities.user.pay.viewmodel.PaymentItem
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.CoinPackage
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackDefault500
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDark900
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.InformationLight300
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel


@Composable
fun CoinPackageChoosingScreen(
    navController: NavController = rememberNavController(),
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = AuthViewModel(),
    payViewModel: PayViewModel
) {
    val authState = authViewModel.authState.observeAsState()

    var coinPackages by remember {
        mutableStateOf<List<CoinPackage>>(emptyList())
    }
    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> {
                coinPackages = FirestoreHelper.getAllCoinPackage()
                coinPackages.sortedBy { it.price }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationItems.Login.route)
            }
            else -> Unit
        }
    }

    Column(modifier = modifier.padding(16.dp)){
        HeaderSection(navController = navController)
        Spacer(modifier = Modifier.height(32.dp))
        LazyRow(Modifier.fillMaxWidth()) {
            items(coinPackages){ item ->
                CoinPackageItem(item, onBuyNowButtonOnClick = {
                    payViewModel.setPaymentItem(PaymentItem.CoinPackageItem(item))
                    navController.navigate(NavigationItems.PaymentMethod.route)
                })
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }

}

@Composable
fun HeaderSection(navController: NavController){
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(id = R.string.back_button_description_text),
                tint = BlackDark900,
                modifier = Modifier.clickable(onClick = {
                    navController.popBackStack()
                })
            )

            Text(
                text = stringResource(id = R.string.coin_package_text),
                style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BlackDark900
            )

            Spacer(modifier = Modifier.width(1.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(id = R.string.get_more_coin_text),
            textAlign = TextAlign.Center,
            style = Typography.titleMedium,
            color = BrandDark900
        )
    }
}

@Composable
fun CoinPackageItem(
    coinPackageItem: CoinPackage,
    onBuyNowButtonOnClick: () -> Unit
){
    Column(
        modifier = Modifier
            .width(250.dp)
            .background(BlackWhite0, RoundedCornerShape(16.dp))
            .border(1.dp, BlackLight300, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Image(
            painter = painterResource(id = R.drawable.coin),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = coinPackageItem.name,
            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = BlackDefault500
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = coinPackageItem.description,
            fontSize = 16.sp,
            color = BlackLight400,
            textAlign = TextAlign.Center
            )

        Spacer(modifier = Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            
            Text(
                text = stringResource(id = R.string.coin_include_text),
                style = Typography.titleLarge,
                color = InformationLight300
            )
            
            Spacer(modifier = Modifier.width(4.dp))

            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = stringResource(
                    id = R.string.image_description_text
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = coinPackageItem.coinValue.toString(),
                style = Typography.headlineSmall,
                color = ErrorDefault500
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row (Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){

            Text(
                text = stringResource(id = R.string.price_text),
                style = Typography.titleLarge,
                color = InformationLight300
            )
            
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = String.format("$" + "%.2f", coinPackageItem.price.toDouble()),
                style = Typography.headlineSmall,
                color = ErrorDefault500
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onBuyNowButtonOnClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandDefault500,
                contentColor = BlackDark900
            )
        ) {
            Text(
                text = stringResource(id = R.string.buy_now_button_text),
                style = Typography.titleMedium
            )
        }
    }
}
