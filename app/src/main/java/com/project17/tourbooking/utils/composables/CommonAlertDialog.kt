package com.project17.tourbooking.utils.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.Typography

@Composable
fun CommonAlertDialog(
    isDialogVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: Int,
    message: Int,
    confirmButtonText: Int,
    dismissButtonText: Int
){
    if(isDialogVisible){
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = title),
                    style = Typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                Text(
                    text = stringResource(id = message),
                    style = Typography.titleMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandDefault500,
                        contentColor = BlackDark900
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(id =confirmButtonText),
                        style = Typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlackWhite0,
                        contentColor = BlackDark900
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BlackLight300)
                ) {
                    Text(
                        text = stringResource(id = dismissButtonText),
                        style = Typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = BlackWhite0,
        )
    }
}