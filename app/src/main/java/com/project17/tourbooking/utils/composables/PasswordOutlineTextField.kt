package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.R
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDark900
import com.project17.tourbooking.ui.theme.Typography

@Composable
fun PasswordOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    interactionSource: MutableInteractionSource,
    isError: Boolean = false,
    errorMessage: String = "",
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = Typography.titleMedium,
                color = BlackLight300
            )
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (interactionSource.collectIsFocusedAsState().value) BrandDefault500 else BlackLight300,
            unfocusedBorderColor = BlackLight300,
            focusedLabelColor = if (interactionSource.collectIsFocusedAsState().value) BrandDefault500 else BlackLight300,
            unfocusedLabelColor = BlackLight300,
            cursorColor = BrandDefault500
        ),
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    painter = painterResource(id = if (isVisible) R.drawable.ic_password_visible else R.drawable.ic_password_invisible),
                    contentDescription = "Toggle password visibility"
                )
            }
        },
        maxLines = 1,
        isError = isError,
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    style = Typography.bodySmall,
                    color = ErrorDark900
                )
            }
        }
    )
}
