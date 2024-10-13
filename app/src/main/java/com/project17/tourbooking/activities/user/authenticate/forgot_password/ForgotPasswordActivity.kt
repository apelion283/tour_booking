package com.project17.tourbooking.activities.user.authenticate.forgot_password
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.google.firebase.auth.FirebaseAuth
import com.project17.tourbooking.R
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDark900
import com.project17.tourbooking.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<Int?>(null) }
    val interactionSourceEmail = remember { MutableInteractionSource() }
    val isFocusedEmail by interactionSourceEmail.collectIsFocusedAsState()
    val coroutineScope = rememberCoroutineScope()
    var isEmailNull by remember {
        mutableStateOf(false)
    }
    
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .verticalScroll(rememberScrollState()),
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = stringResource(id = R.string.input_your_email_text),
                style = Typography.headlineMedium,
                color = BlackLight300,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.forgot_password_text),
                style = Typography.headlineLarge,
                color = BlackDark900,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = Int.MAX_VALUE
                    isEmailNull = false
                                },
                label = { Text(stringResource(id = R.string.email_text), style = Typography.titleMedium, color = BlackLight300)},
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                interactionSource = interactionSourceEmail,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isFocusedEmail) BrandDefault500 else BlackLight300,
                    unfocusedBorderColor = BlackLight300,
                    focusedLabelColor = if (isFocusedEmail) BrandDefault500 else BlackLight300,
                    unfocusedLabelColor = BlackLight300,
                    cursorColor = BrandDefault500,
                ),
                isError = isEmailNull,
                supportingText = {
                    if (isEmailNull) {
                        Text(
                            text = stringResource(id = R.string.email_is_required_error_text),
                            style = Typography.bodyLarge,
                            color = ErrorDark900)
                    }
                },
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            emailError?.let {
                Text(
                    text = if(it != Int.MAX_VALUE) stringResource(id = it) else "",
                    color = ErrorDark900,
                    style = Typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (email.trim().isEmpty()) {
                        isEmailNull = true
                    } else if (!email.trim().contains("@")) {
                        emailError = R.string.please_enter_a_valid_email_text
                    } else {
                        coroutineScope.launch {
                            val auth = FirebaseAuth.getInstance()

                            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val signInMethods = task.result?.signInMethods
                                    if (!signInMethods.isNullOrEmpty()) {
                                        auth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener { sendTask ->
                                                if (sendTask.isSuccessful) {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.reset_password_link_sent_text),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    emailError = Int.MAX_VALUE
                                                    navController.popBackStack()
                                                } else {
                                                    emailError = R.string.email_is_disabled_text
                                                }
                                            }
                                    } else {
                                        emailError = R.string.email_not_found_text
                                    }
                                } else {
                                    emailError = R.string.something_went_wrong_text
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
                    .imePadding(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDefault500,
                    contentColor = BlackDark900
                )
            ) {
                Text(stringResource(id = R.string.submit_button_text), style = Typography.headlineSmall)
            }
        }

        IconButton(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back")
        }
    }
}
