package com.project17.tourbooking.activities.user.authenticate.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.ACCOUNT_ROLE
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDark900
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CustomOutlinedTextField
import com.project17.tourbooking.utils.composables.PasswordOutlinedTextField
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val interactionSourceEmail = remember { MutableInteractionSource() }
    val interactionSourcePassword = remember { MutableInteractionSource() }


    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                navController.navigate(NavigationItems.Home.route)
            }

            is AuthState.Error -> loginError = context.getString(R.string.login_error_text)
            else -> Unit
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(BlackWhite0),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,


            ) {
            HeaderSection(navController = navController)
            AppLogo()
            Spacer(modifier = Modifier.weight(.1f))
            CustomOutlinedTextField(
                value = emailOrUsername,
                onValueChange = {
                    emailOrUsername = it
                    isEmailError = false
                    loginError = ""
                },
                label = stringResource(id = R.string.email_text),
                interactionSource = interactionSourceEmail,
                isError = isEmailError,
                errorMessage = loginError,
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordOutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    isPasswordError = false
                    loginError = ""
                },
                label = stringResource(id = R.string.password_text),
                isVisible = isPasswordVisible,
                onVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                interactionSource = interactionSourcePassword,
                isError = isPasswordError,
                errorMessage = loginError
            )
            LoginErrorText(loginError = loginError)
            RememberMeAndForgotPasswordSection(
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
                onForgotPasswordClick = { navController.navigate(NavigationItems.ForgotPassword.route) }
            )
            Spacer(modifier = Modifier.weight(.5f))
            LoginButton(
                onClick = {
                    isEmailError = emailOrUsername.trim().isEmpty()
                    isPasswordError = password.trim().isEmpty()

                    if (!isEmailError && !isPasswordError) {
                        authViewModel.login(emailOrUsername.trim(), password) { result ->
                            if (result == null) {
                                coroutineScope.launch {
                                    val userRole = FirestoreHelper.getUserRole(authViewModel.getCurrentUser()?.uid!!)
                                    if (userRole != null) {
                                        if(userRole == ACCOUNT_ROLE.ADMIN.toString() || userRole == ACCOUNT_ROLE.STAFF.toString()){
                                            navController.navigate(NavigationItems.ManageOverview.route)
                                        }
                                        else{
                                            navController.navigate(NavigationItems.Home.route)
                                        }
                                    }
                                }
                            } else {
                                loginError = context.getString( R.string.login_error_text)
                            }
                        }
                    }
                    else if (isEmailError && isPasswordError){
                        loginError = context.getString(R.string.this_field_is_required_text)
                    }
                    else if(isEmailError){
                        loginError = context.getString(R.string.email_username_error_text)
                    }
                    else {
                        loginError = context.getString(R.string.password_error_text)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            CreateAccountButton(onClick = { navController.navigate(NavigationItems.InputFullName.route) })
            Spacer(modifier = Modifier.height(32.dp))
            SocialLoginButtons()
            Spacer(modifier = Modifier.height(32.dp))
        }

    }
}

@Composable
fun HeaderSection(navController: NavController){
    Row (Modifier.fillMaxWidth()){
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_home),
            contentDescription = "",
            modifier = Modifier
                .size(35.dp)
                .clickable { navController.navigate(NavigationItems.Home.route) }
        )
        Spacer(modifier = Modifier.weight(.01f))
    }
}

@Composable
fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = stringResource(id = R.string.image_description_text),
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
            .fillMaxWidth(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun LoginErrorText(loginError: String?) {
    loginError?.let {
        Text(
            text = if(loginError.equals(stringResource(id = R.string.login_error_text))) loginError else "",
            color = ErrorDark900,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun RememberMeAndForgotPasswordSection(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked
                = rememberMe,
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = BrandDefault500,
                    uncheckedColor = BlackLight300
                )
            )
            Text(
                text = stringResource(id = R.string.remember_me_text),
                style = Typography.bodyLarge,
                color = BlackLight400
            )
        }

        TextButton(onClick = onForgotPasswordClick) {
            Text(
                text = stringResource(id = R.string.forgot_password_text),
                style = Typography.bodyLarge,
                color = ErrorDefault500
            )
        }
    }
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandDefault500,
            contentColor = BlackDark900
        )
    ) {
        Text(
            text = stringResource(id = R.string.login_button_text),
            style = Typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun CreateAccountButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = BlackLight300,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BlackWhite0,
            contentColor = BlackDark900,
        )
    ) {
        Text(
            text = stringResource(id = R.string.create_account_text),
            style = Typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun SocialLoginButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_facebook),
            contentDescription = "Facebook Login",
            modifier = Modifier
                .size(64.dp)
                .clickable { /* Xử lý đăng nhập bằng Facebook */ }
                .background(BlackLight100, RoundedCornerShape(48.dp))
                .padding(8.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_instagram),
            contentDescription = "Instagram Login",
            modifier = Modifier
                .size(64.dp)
                .background(BlackLight100, RoundedCornerShape(48.dp))
                .padding(8.dp)
                .clickable { /* Xử lý đăng nhập bằng Instagram */ }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google Login",
            modifier = Modifier
                .size(64.dp)
                .background(BlackLight100, RoundedCornerShape(48.dp))
                .padding(8.dp)
                .clickable { /* Xử lý đăng nhập bằng Google */ }
        )
    }
}