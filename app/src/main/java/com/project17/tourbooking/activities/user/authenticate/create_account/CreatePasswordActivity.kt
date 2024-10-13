package com.project17.tourbooking.activities.user.authenticate.create_account
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.PasswordOutlinedTextField
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class CreatePasswordActivity: ComponentActivity() {
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
fun CreatePasswordScreen(
    navController: NavController = rememberNavController(),
    fullName: String = "",
    userName: String = "",
    email: String = "",
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {

    var password by remember { mutableStateOf("") }
    var isPasswordError by remember { mutableStateOf(false) }
    val passwordInteractSource = remember {
        MutableInteractionSource()
    }
    var errorMessages by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }
    val authState by authViewModel.authState.observeAsState(initial = AuthState.Unauthenticated)
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignUpSuccess -> {
                navController.navigate(NavigationItems.Home.route) {
                    popUpTo(NavigationItems.Login.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState as AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(NavigationItems.Login.route)
            }

            AuthState.Authenticated -> {

            }
            AuthState.Loading -> {
            }
            AuthState.Unauthenticated -> {

            }
        }
    }

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
            text = stringResource(id = R.string.create_a_password_text),
            style = Typography.headlineMedium,
            color = BlackDark900
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordOutlinedTextField(
            value = password,
            onValueChange = {
                isPasswordError = false
                password = it
            },
            label = stringResource(id = R.string.password_text),
            modifier = Modifier.fillMaxWidth(),
            interactionSource = passwordInteractSource,
            isError = isPasswordError,
            errorMessage = errorMessages,
            isVisible = isPasswordVisible,
            onVisibilityToggle = {
                isPasswordVisible = !isPasswordVisible
            }
        )


        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (password.isEmpty()) {
                    errorMessages = context.getString(R.string.this_field_is_required_text)
                    isPasswordError = true
                } else {
                    val isWeakPassword = password.length < 6
                    if (isWeakPassword) {
                        isPasswordError = true
                        errorMessages = context.getString(R.string.password_rule_text)
                    } else {
                        if (fullName.isNotEmpty() && userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            coroutineScope.launch {
                                authViewModel.signUp(
                                    fullName = fullName,
                                    userName = userName,
                                    email = email,
                                    password = password
                                )
                                navController.navigate(NavigationItems.AccountCreated.route)
                            }
                        }
                    }
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
                stringResource(id = R.string.submit_button_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
