package com.project17.tourbooking.activities.user.profile.profile

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.DEFAULT_AVATAR
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Account
import com.project17.tourbooking.models.Customer
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import com.project17.tourbooking.utils.composables.RequireLogin
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var customer by remember{ mutableStateOf(Customer()) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var account by remember {
        mutableStateOf<Account?>(null)
    }

    var isDataLoaded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated, AuthState.SignUpSuccess -> {
                if (!isDataLoaded) {
                    val currentUser = authViewModel.getCurrentUser()
                    FirestoreHelper.getAvatarUrlFromAccountId(currentUser!!.uid) {
                        avatarUrl = it
                    }
                    val customerId = FirestoreHelper.getCustomerIdByAccountId(currentUser.uid)
                    if (customerId.getOrNull() != null) {
                        FirestoreHelper.getCustomerInfoByCustomerId(customerId.getOrNull()!!) {
                            if (it != null) {
                                customer = it
                                isDataLoaded = true
                            }
                        }
                        account = FirestoreHelper.getAccountByAccountId(currentUser.uid)
                    }
                }
            }

            is AuthState.Error -> {}

            is AuthState.Unauthenticated -> {}

            else -> Unit
        }
    }

    if (authState.value is AuthState.Unauthenticated) {
        RequireLogin(navController)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackWhite0)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {



            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.your_profile_text),
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    Modifier
                        .border(1.dp, BlackLight300, RoundedCornerShape(16.dp))
                        .background(BlackLight100, RoundedCornerShape(16.dp))
                        .clickable { navController.navigate(NavigationItems.CoinPackageBooking.route) },
                    verticalAlignment = Alignment.CenterVertically

                ){
                    Row(Modifier
                        .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.coin),
                            contentDescription = stringResource(
                                id = R.string.image_description_text
                            ),
                            modifier = Modifier
                                .size(30.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = String.format("%d",account?.coin ?: 0),
                            style = Typography.headlineSmall,
                            color = ErrorDefault500
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = avatarUrl ?: DEFAULT_AVATAR
                    ),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(72.dp))
                        .border(1.dp, BlackLight300, RoundedCornerShape(50.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Hello, ${account?.userName ?: stringResource(id = R.string.default_username)}", style = Typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(customer.address, style = Typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                R.string.personal_information_text,
                R.drawable.ic_person,
                onClick = {
                    navController.navigate(NavigationItems.PersonalInformation.route)
                })

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                R.string.change_your_password_text,
                R.drawable.ic_password,
                onClick = {
                    navController.navigate(NavigationItems.ChangePassword.route)
                })
            
            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(
                R.string.notification_text,
                R.drawable.ic_notification,
                onClick = {
                    navController.navigate(NavigationItems.Notification.route)
                })

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(R.string.faq_text, R.drawable.ic_help, onClick = {
                navController.navigate(NavigationItems.FAQ.route)
            })

            Spacer(modifier = Modifier.height(16.dp))

            ProfileCard(R.string.logout_text, R.drawable.ic_logout) {
                showLogoutDialog = true
            }

            CommonAlertDialog(
                onDismiss = { showLogoutDialog = false },
                title = R.string.logout_text,
                onConfirm = {
                    authViewModel.signOut()
                    navController.navigate(NavigationItems.Home.route)
                    showLogoutDialog = false
                },
                message = R.string.logout_message_text,
                confirmButtonText = R.string.logout_button_text,
                dismissButtonText = R.string.cancel_button_text,
                isDialogVisible = showLogoutDialog
            )
        }
    }
}

@Composable
fun ProfileCard(title: Int, iconResId: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlackLight300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(id = title), style = Typography.titleLarge)
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = title),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FAQWebView(){
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadUrl("https://github.com/DNhat283N")
        }
    })
}