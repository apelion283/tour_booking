package com.project17.tourbooking.activities.user.authenticate.create_account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CustomOutlinedTextField
import kotlinx.coroutines.launch

class NameInputActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NameInputScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NameInputScreen(
    navController: NavController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var userName by remember {
        mutableStateOf("")
    }
    var isFirstNameError by remember { mutableStateOf(false) }
    var isLastNameError by remember { mutableStateOf(false) }
    var isUserNameError by remember {
        mutableStateOf(false)
    }
    var userNameErrorMessage by remember {
        mutableStateOf("")
    }
    val firstNameInteractSource = remember {
        MutableInteractionSource()
    }
    val lastNameInteractSource = remember {
        MutableInteractionSource()
    }
    val userNameInteractionSource = remember{
        MutableInteractionSource()
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center,
    ) {

        HeaderSection(navController = navController)

        Text(
            text = stringResource(id = R.string.create_account_text),
            style = Typography.headlineSmall,
            color = BlackLight300
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.what_is_your_name_text),
            style = Typography.headlineMedium,
            color = BlackDark900
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomOutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                isFirstNameError = false
                            },
            label = stringResource(id = R.string.first_name_text),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = firstNameInteractSource,
            isError = isFirstNameError,
            errorMessage = stringResource(id = R.string.this_field_is_required_text),
            imeAction = ImeAction.Next
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomOutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                isLastNameError = false
            },
            label = stringResource(id = R.string.last_name_text),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = lastNameInteractSource,
            isError = isLastNameError,
            errorMessage = stringResource(id = R.string.this_field_is_required_text),
            imeAction = ImeAction.Next
        )

        Text(
            text = stringResource(id = R.string.your_user_name_is_text),
            style = Typography.headlineMedium,
            color = BlackDark900
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomOutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                isUserNameError = false
            },
            label = stringResource(id = R.string.user_name_text),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = userNameInteractionSource,
            isError = isUserNameError,
            errorMessage = userNameErrorMessage,
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                isLastNameError = lastName.isEmpty()
                isFirstNameError = firstName.isEmpty()
                isUserNameError = userName.isEmpty()

                if (!isLastNameError && !isFirstNameError && !isUserNameError) {
                    coroutineScope.launch {
                        val isUserNameExist = FirestoreHelper.isUsernameExists(userName)
                        if(!isUserNameExist){
                            val fullName = "$lastName $firstName"
                            navController.navigate(NavigationItems.InputEmail.route + "/$fullName/$userName")
                        }
                        else{
                            userNameErrorMessage = context.getString(R.string.username_already_exists_text)
                            isUserNameError = true
                        }
                    }

                }else if(isUserNameError){
                    userNameErrorMessage = context.getString(R.string.this_field_is_required_text)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .imePadding(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandDefault500,
                contentColor = BlackDark900
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                stringResource(id = R.string.input_your_email_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun HeaderSection(navController: NavController) {
    Row(Modifier.fillMaxWidth()) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            modifier = Modifier
                .size(25.dp)
                .clickable {
                    navController.popBackStack()
                }
        )

        Spacer(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(32.dp))

}
