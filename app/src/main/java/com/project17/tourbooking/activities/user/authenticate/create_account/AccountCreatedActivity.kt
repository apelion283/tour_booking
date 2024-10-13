package com.project17.tourbooking.activities.user.authenticate.create_account

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.Typography

@Composable
fun AccountCreatedScreen (navController: NavController){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) { Spacer(modifier = Modifier.height(150.dp))

        BackHandler {
            navController.navigate(NavigationItems.Home.route) {
                popUpTo(NavigationItems.Home.route) {
                    inclusive = true
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_location_2),
                contentDescription = "Successful Create Account",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.successfully_created_account_text),
            style = Typography.headlineMedium,
            color = BlackDark900,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(id = R.string.after_this_text),
            style = Typography.bodyLarge,
            color = BlackLight300,
            modifier = Modifier.padding(start = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { navController.navigate(NavigationItems.Home.route) {
                popUpTo(NavigationItems.Home.route) {
                    inclusive = true
                }
            } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandDefault500,
                contentColor = BlackDark900
            )
        ) {
            Text(
                stringResource(id = R.string.explored_button_text),
                style = Typography.headlineSmall,
            )
        }
    }
}

@Preview
@Composable
fun AccountCreatedScreenPreview() {
    AccountCreatedScreen(navController = rememberNavController())
}