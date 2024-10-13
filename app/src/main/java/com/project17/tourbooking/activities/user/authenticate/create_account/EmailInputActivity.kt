package com.project17.tourbooking.activities.user.authenticate.create_account
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CustomOutlinedTextField
import kotlinx.coroutines.launch

class EmailInputActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EmailInputScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EmailInputScreen(
    navController: NavController = rememberNavController(),
    fullName: String = "",
    userName: String = "",
    modifier: Modifier = Modifier
) {

    var email by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    val emailInteractSource = remember {
        MutableInteractionSource()
    }
    var errorMessages by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement = Arrangement.Center
    ) {

        HeaderSection(navController = navController)

        Text(
            text = stringResource(id = R.string.create_your_account_text),
            style = Typography.headlineSmall,
            color = BlackLight300
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.and_your_email_text),
            style = Typography.headlineMedium,
            color = BlackDark900
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomOutlinedTextField(
            value = email,
            onValueChange = {
                isEmailError = false
                email = it
                            },
            label = stringResource(id = R.string.email_text),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = emailInteractSource,
            isError = isEmailError,
            errorMessage = errorMessages,
            imeAction = ImeAction.Done
        )


        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if(email.isEmpty()){
                    errorMessages = context.getString(R.string.this_field_is_required_text)
                    isEmailError = true
                }else if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.length > 10){
                    coroutineScope.launch {
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener{task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result?.signInMethods
                                val isEmailExists = !signInMethods.isNullOrEmpty()

                                if(!isEmailExists) {
                                    if(fullName.isNotEmpty() && userName.isNotEmpty()){
                                        navController.navigate(NavigationItems.CreatePassword.route + "/$fullName/$userName/$email")
                                    }
                                    else {
                                        errorMessages = context.getString(R.string.something_went_wrong_text)
                                        isEmailError = true
                                    }
                                }
                                else{
                                    errorMessages = context.getString(R.string.email_already_exists_text)
                                    isEmailError = true
                                }
                            }
                            else{
                                errorMessages = context.getString(R.string.email_already_exists_text)
                                isEmailError = true
                            }
                        }

                    }
                }
                else if(email.length <= 10){
                    errorMessages = context.getString(R.string.email_is_too_short_text)
                    isEmailError = true
                }
                else{
                    errorMessages = context.getString(R.string.email_is_invalid_text)
                    isEmailError = true
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
