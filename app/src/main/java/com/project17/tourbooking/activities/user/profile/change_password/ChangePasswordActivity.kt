package com.project17.tourbooking.activities.user.profile.change_password

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.PasswordOutlinedTextField
import com.project17.tourbooking.viewmodels.AuthViewModel

class ChangePasswordActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChangePasswordScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class PasswordFieldState(
    var value: String = "",
    var isVisible: Boolean = false,
    var isError: Boolean = false,
    var errorMessage: String = ""
)

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
){
    val context = LocalContext.current
    var oldPasswordState by remember { mutableStateOf(PasswordFieldState()) }
    var newPasswordState by remember { mutableStateOf(PasswordFieldState()) }
    var confirmPasswordState by remember { mutableStateOf(PasswordFieldState()) }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)) {
        NavBarSection(navController = navController)

        Spacer(modifier = Modifier.height(32.dp))

        PasswordField(
            state = oldPasswordState,
            onStateChange = { oldPasswordState = it },
            label = stringResource(
                id = R.string.your_current_password_text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            state = newPasswordState,
            onStateChange = { newPasswordState = it },
            label = stringResource(id = R.string.input_your_new_password_text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            state = confirmPasswordState,
            onStateChange = { confirmPasswordState = it },
            label = stringResource(id = R.string.confirm_password_text)
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
             val (isValid, updateState) = validatePasswords(oldPasswordState, newPasswordState, confirmPasswordState, context)
                oldPasswordState = updateState[0]
                newPasswordState = updateState[1]
                confirmPasswordState = updateState[2]
                if (isValid) {
                    authViewModel.changePassword(newPasswordState.value)
                    Toast.makeText(context, context.getString(R.string.password_changed_successfully_text), Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandDefault500,
                contentColor = BlackDark900
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.submit_button_text), style = Typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PasswordField(
    state: PasswordFieldState,
    onStateChange: (PasswordFieldState) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Next
) {
    PasswordOutlinedTextField(
        value = state.value,
        onValueChange = { onStateChange(state.copy(value = it, isError = false)) },
        label = label,
        isVisible = state.isVisible,
        onVisibilityToggle = { onStateChange(state.copy(isVisible = !state.isVisible)) },
        interactionSource = remember { MutableInteractionSource() },
        isError = state.isError,
        errorMessage = state.errorMessage,
        imeAction = imeAction
    )
}

fun validatePasswords(
    oldPassword: PasswordFieldState,
    newPassword: PasswordFieldState,
    confirmPassword: PasswordFieldState,
    context: Context
): Pair<Boolean, List<PasswordFieldState>> {
    val updatedOldPassword = oldPassword.copy(
        isError = oldPassword.value.isEmpty(),
        errorMessage = if (oldPassword.value.isEmpty()) context.getString(R.string.this_field_is_required_text) else ""
    )
    val updatedNewPassword = newPassword.copy(
        isError = newPassword.value.isEmpty() || newPassword.value != confirmPassword.value,
        errorMessage = when {
            newPassword.value.isEmpty() -> context.getString(R.string.this_field_is_required_text)
            newPassword.value != confirmPassword.value -> context.getString(R.string.password_does_not_match_text)
            else -> ""
        }
    )
    val updatedConfirmPassword = confirmPassword.copy(
        isError = confirmPassword.value.isEmpty() || newPassword.value != confirmPassword.value,
        errorMessage = when {
            confirmPassword.value.isEmpty() -> context.getString(R.string.this_field_is_required_text)
            newPassword.value != confirmPassword.value -> context.getString(R.string.password_does_not_match_text)
            else -> ""
        }
    )

    val isValid = !updatedOldPassword.isError && !updatedNewPassword.isError && !updatedConfirmPassword.isError

    return Pair(isValid, listOf(updatedOldPassword, updatedNewPassword, updatedConfirmPassword))
}

@Composable
fun NavBarSection(
    navController: NavController
) {
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            tint = BlackDark900,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { navController.popBackStack() }
        )
        Text(
            text = stringResource(id = R.string.change_your_password_text),
            style = Typography.titleLarge,
            color = BlackDark900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(0.dp))
    }
}
