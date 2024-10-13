package com.project17.tourbooking.activities.admin.coin_packages

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.CoinPackage
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import kotlinx.coroutines.launch

@Composable
fun ManageCoinPackageScreen(navController: NavController) {
    val coinPackages = remember { mutableStateListOf<CoinPackage>() }
    var isEditing by remember { mutableStateOf(false) }
    var editingCoinPackageId by remember { mutableStateOf<String?>(null) }
    var editingCoinPackage by remember { mutableStateOf<CoinPackage?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var coinPackageToDelete by remember{ mutableStateOf<CoinPackage?>(null) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember{ mutableStateOf(false) }
    var isAbleToBack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        isAbleToBack = true
        coinPackages.addAll(FirestoreHelper.getAllCoinPackage())
        isLoading = false
    }

    if (isDeleteDialogVisible && coinPackageToDelete != null) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = {
                isDeleteDialogVisible = false
                coinPackageToDelete = null
            },
            onConfirm = {
                isDeleteDialogVisible = false
                isLoading = true
                isAbleToBack = false
                coroutineScope.launch {
                    FirestoreHelper.deleteCoinPackageByCoinPackageId(coinPackageToDelete!!.id)
                    coinPackages.clear()
                    coinPackages.addAll(FirestoreHelper.getAllCoinPackage())
                    isLoading = false
                    isAbleToBack = !isAbleToBack
                }
            },
            title = R.string.delete_coin_package_question_text,
            message = R.string.are_your_sure_text,
            confirmButtonText = R.string.confirm_button_text,
            dismissButtonText = R.string.cancel_button_text
        )
    }

    if(!isAbleToBack) BackHandler {}

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { if (isAbleToBack) navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.manage_coin_package_text), style = Typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        if (isEditing) {
            EditingCoinPackageForm(
                coinPackage = editingCoinPackage,
                isLoading = isLoading,
                onSave = { name, description, coinValue, price ->
                    coroutineScope.launch {
                        isLoading = true
                        isAbleToBack = false
                        editingCoinPackage = CoinPackage(
                            id = "",
                            name = name,
                            coinValue = coinValue,
                            price = price,
                            description = description,
                        )
                        isEditing = false
                        editingCoinPackageId?.let { FirestoreHelper.updateCoinPackageByCoinPackageId(it,
                            editingCoinPackage!!
                        ) }
                        coinPackages.clear()
                        coinPackages.addAll(FirestoreHelper.getAllCoinPackage())
                        Toast.makeText(context, context.getString(R.string.all_change_saved_text), Toast.LENGTH_SHORT).show()
                        isLoading = false
                        isAbleToBack = true
                    }
                },
                onCancel = {
                    isEditing = false
                    editingCoinPackageId = null
                    editingCoinPackage = null
                }
            )
        } else {
            AddNewDestinationForm(isLoading) { name, description, coinValue, price ->
                coroutineScope.launch {
                    isLoading = true
                    isAbleToBack = false

                    val coinPackageToAdd = CoinPackage(
                        id = "",
                        name = name,
                        description = description,
                        coinValue = coinValue,
                        price = price
                    )

                    FirestoreHelper.createCoinPackage(coinPackageToAdd)
                    coinPackages.clear()
                    coinPackages.addAll(FirestoreHelper.getAllCoinPackage())

                    isLoading = false
                    isAbleToBack = true
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if(isLoading){
            CircularProgressIndicator()
        }
        else{
            LazyColumn {
                items(coinPackages) { coinPackage ->
                    CoinPackageItem(
                        coinPackage = coinPackage,
                        onEdit = {
                            isEditing = true
                            editingCoinPackageId = coinPackage.id
                            editingCoinPackage = coinPackage
                        },
                        onDelete = {
                            coinPackageToDelete = coinPackage
                            isDeleteDialogVisible = true
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun AddNewDestinationForm(isLoading: Boolean, onAddCategory: (String, String, Int, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coinValue by remember{ mutableIntStateOf(0) }
    var price by remember{ mutableDoubleStateOf(0.0) }
    val context = LocalContext.current

    Column {

        Text(text = stringResource(id = R.string.add_new_coin_package_text), style = Typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = name,
            onValueChange = { name = it },
            readOnly = isLoading,
            modifier = Modifier
                .border(1.dp, BlackLight300)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            readOnly = isLoading,
            modifier = Modifier
                .border(1.dp, BlackLight300)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = stringResource(
                    id = R.string.image_description_text
                ),
                modifier = Modifier
                    .size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = coinValue.toString(),
                readOnly = isLoading,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                    coinValue = newValue.toIntOrNull() ?: 0
                } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .border(1.dp, BlackLight300)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format("$"),
                style = Typography.bodySmall,
                color = ErrorDefault500
            )
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = price.toString(),
                readOnly = isLoading,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }  ) {
                        price = newValue.toDoubleOrNull() ?: 0.0
                    } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .border(1.dp, BlackLight300)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                name.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.destination_location_empty_text), Toast.LENGTH_SHORT).show()
                }
                description.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.destination_description_empty_text), Toast.LENGTH_SHORT).show()
                }
                coinValue <= 0 -> {
                    Toast.makeText(context, context.getString(R.string.coin_value_invalid_text), Toast.LENGTH_SHORT).show()
                }
                price <= 0 -> {
                    Toast.makeText(context, context.getString(R.string.coin_price_invalid_text), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    onAddCategory(name, description, coinValue, price)
                    name = ""
                    description = ""
                    coinValue = 0
                    price = 0.0
                }
            }
        }, enabled = !isLoading) {
            Text(stringResource(id = R.string.add_new_coin_package_text))
        }
    }
}

@Composable
fun EditingCoinPackageForm(
    coinPackage: CoinPackage?,
    isLoading: Boolean,
    onSave: (String, String, Int, Double) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(coinPackage?.name ?: "") }
    var description by remember { mutableStateOf(coinPackage?.description ?: "") }
    var coinValue by remember { mutableIntStateOf(coinPackage?.coinValue ?: 0) }
    var price by remember { mutableDoubleStateOf(coinPackage?.price ?: 0.0) }
    val context = LocalContext.current

    Column {
        Text(text = stringResource(id = R.string.edit_coin_package_text), style = Typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = name,
            readOnly = isLoading,
            onValueChange = { name = it },
            modifier = Modifier
                .border(1.dp, BlackLight300)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = description,
            readOnly = isLoading,
            onValueChange = { description = it },
            modifier = Modifier
                .border(1.dp, BlackLight300)
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = stringResource(
                    id = R.string.image_description_text
                ),
                modifier = Modifier
                    .size(15.dp)
            )
            BasicTextField(
                value = coinValue.toString(),
                readOnly = isLoading,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        coinValue = newValue.toIntOrNull() ?: 0
                    } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .border(1.dp, BlackLight300)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format("$ "),
                style = Typography.bodySmall,
                color = ErrorDefault500
            )
            BasicTextField(
                value = price.toString(),
                readOnly = isLoading,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        price = newValue.toDoubleOrNull() ?: 0.0
                    } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .border(1.dp, BlackLight300)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                when{
                    name.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.destination_location_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    description.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.destination_description_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    coinValue <= 0 -> {
                        Toast.makeText(context, context.getString(R.string.coin_value_invalid_text), Toast.LENGTH_SHORT).show()
                    }
                    price <= 0 -> {
                        Toast.makeText(context, context.getString(R.string.coin_price_invalid_text), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        onSave(name, description, coinValue, price)
                    }
                }
            }, enabled = !isLoading) {
                Text(stringResource(id = R.string.save_text))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onCancel, enabled = !isLoading) {
                Text(stringResource(id = R.string.cancel_button_text))
            }
        }
    }
}


@Composable
fun CoinPackageItem(coinPackage: CoinPackage, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(BlackLight300)
            .border(1.dp, BlackLight300)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = coinPackage.name, style = Typography.bodyLarge)
                Text(text = coinPackage.description, style = Typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = stringResource(
                            id = R.string.image_description_text
                        ),
                        modifier = Modifier
                            .size(15.dp)
                    )
                    Text(
                        text = String.format("%d", (coinPackage.coinValue)),
                        style = Typography.bodySmall,
                        color = ErrorDefault500
                    )
                }
                Text(
                    text = String.format("$" + "%.2f", (coinPackage.price)),
                    style = Typography.bodySmall,
                    color = ErrorDefault500
                )
            }
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}