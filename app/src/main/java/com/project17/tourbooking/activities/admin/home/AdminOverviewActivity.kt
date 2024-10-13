package com.project17.tourbooking.activities.admin.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel

@Composable
fun ManageOverviewScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {}
            is AuthState.Error -> {
                navController.navigate(NavigationItems.Login.route)
            }
            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationItems.Login.route)
            }
            else -> Unit
        }
    }

    if (authState.value is AuthState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(id = R.string.manage_overview_text), style = Typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.ManageTour.route) }) {
                Text(stringResource(id = R.string.manage_tour_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.ManageCoinPackage.route) }) {
                Text(stringResource(id = R.string.manage_coin_package_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.ManageAccount.route) }) {
                Text(stringResource(id = R.string.manage_account_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.ManageCategory.route) }) {
                Text(stringResource(id = R.string.manage_category_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.ManageDestination.route) }) {
                Text(stringResource(id = R.string.manage_destination_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate(NavigationItems.Statistic.route) }) {
                Text(stringResource(id = R.string.statistic_report_text))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate(NavigationItems.Login.route) {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.logout_text))
            }
        }
    }
}
