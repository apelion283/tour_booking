package com.project17.tourbooking.activities.user.pay.presentation.tour_booking

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.activities.user.pay.viewmodel.PayViewModel
import com.project17.tourbooking.activities.user.pay.viewmodel.PaymentItem
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight200
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel


@Composable
fun BookingDetailScreen(
    navController: NavController,
    tourId: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = AuthViewModel(),
    payViewModel: PayViewModel = PayViewModel()
) {
    var tour by remember { mutableStateOf<Tour?>(null) }
    var customerName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    var ticket by remember { mutableStateOf<Ticket?>(null) }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                tour = FirestoreHelper.getTourByTourId(tourId)
                ticket = FirestoreHelper.getTicketOfTourByTourId(tourId)
                val currentUser = authViewModel.getCurrentUser()
                FirestoreHelper.getCustomerNameByAccountId(currentUser?.uid!!) {
                    customerName = it ?: ""
                }
                contactInfo = currentUser?.email!!
            }

            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                Log.e("BookingDetailScreen", errorMessage)
            }

            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationItems.Login.route)
            }

            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )

            NavBarSection(navController)
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
            )
            BookingForm(
                customerName = customerName,
                contactInfo = contactInfo,
                onCustomerNameChanged = { customerName = it },
                onContactInfoChanged = { contactInfo = it },
                onQuantityChanged = { quantity = it },
                slotQuantity = tour?.slotQuantity?.minus(tour?.bookingCount!!) ?: 0
            )
        }

        val price = Pair(ticket?.moneyPrice?.toDouble(), ticket?.coinPrice?.toInt())
        FooterSection(
            price = price,
            quantity = quantity,
            onConfirmClick = {
               if(ticket == null){
                   Toast.makeText(context, context.getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show()
                   navController.popBackStack()
               }
                else{
                   payViewModel.setPaymentItem(PaymentItem.TicketItem(ticket!!))
                   payViewModel.setQuantity(quantity)
                   navController.navigate(NavigationItems.PaymentMethod.route)
               }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun NavBarSection(
    navController: NavController = rememberNavController()
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
            text = stringResource(id = R.string.title_activity_detail_booking_text),
            style = Typography.titleLarge,
            color = BlackDark900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BookingForm(
    customerName: String,
    contactInfo: String,
    onCustomerNameChanged: (String) -> Unit,
    onContactInfoChanged: (String) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    slotQuantity: Int
) {
    var showErrorName by remember { mutableStateOf(false) }
    var showErrorContact by remember { mutableStateOf(false) }
    var errorMessageName by remember { mutableStateOf<String?>(null) }
    var errorMessageContact by remember { mutableStateOf<String?>(null) }

    val validSlotQuantity = if (slotQuantity > 0) slotQuantity else 1

    Column {
        InformationTextField(
            label = R.string.person_responsible_text,
            value = customerName,
            onValueChange = { newValue ->
                onCustomerNameChanged(newValue)
                if (showErrorName) {
                    showErrorName = false
                }
            },
            errorMessage = errorMessageName,
            showError = showErrorName
        )

        Spacer(modifier = Modifier.height(16.dp))
        InformationTextField(
            label = R.string.contact_info_text,
            value = contactInfo,
            onValueChange = { newValue ->
                onContactInfoChanged(newValue)
                if (showErrorContact) {
                    showErrorContact = false
                }
            },
            errorMessage = errorMessageContact,
            showError = showErrorContact,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        val dropdownList =
            (1..validSlotQuantity).map { "$it " + stringResource(id = R.string.member_text) }
        DropdownMenuWithOptions(
            options = dropdownList,
            onOptionSelected = { newValue -> onQuantityChanged(newValue + 1) }
        )
    }
}


@Composable
fun InformationTextField(
    label: Int,
    value: String = "",
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    showError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        readOnly = true,
        onValueChange = { newValue -> onValueChange(newValue) },
        label = { Text(stringResource(id = label)) },
        singleLine = true,
        isError = showError,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = if (showError) Color.Red else BlackLight200,
            focusedBorderColor = if (showError) Color.Red else BlackDark900,
            focusedLabelColor = if (showError) Color.Red else BlackDark900
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        )
    )
    if (showError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = Typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuWithOptions(options: List<String>, onOptionSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options.getOrElse(0) { "No options available" }) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            singleLine = true,
            label = { Text(stringResource(id = R.string.member_text)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(id = R.string.image_description_text),
                    Modifier.clickable { expanded = expanded }
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BlackLight200,
                focusedBorderColor = BlackDark900,
                focusedLabelColor = BlackDark900
            ),
            shape = RoundedCornerShape(16.dp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(BlackWhite0)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = "No options available") },
                    onClick = { expanded = false },
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption) },
                        onClick = {
                            selectedOptionText = selectionOption
                            expanded = false
                            onOptionSelected(index)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun FooterSection(
    price: Pair<Double?, Int?>,
    quantity: Int,
    onConfirmClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlackWhite0),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = stringResource(id = R.string.total_text),
                    color = BlackLight400,
                    style = Typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    if(price.first != null){
                        Row {
                            Text(
                                text = String.format("$" + "%.2f", (price.first!!.times(quantity))),
                                style = Typography.headlineMedium,
                                color = ErrorDefault500
                            )
                        }
                    }
                    if(price.second != null){
                        Row {
                            if(price.second != null){
                                Text(
                                    text = "or",
                                    style = Typography.titleSmall,
                                    color = BlackLight300
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = stringResource(
                                        id = R.string.image_description_text
                                    ),
                                    modifier = Modifier
                                        .size(20.dp)
                                )

                                Text(
                                    text = String.format("%d", (price.second!!.times(quantity))),
                                    style = Typography.headlineMedium,
                                    color = ErrorDefault500
                                )
                            }

                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirmClick() },
                colors = ButtonColors(BrandDefault500 , BlackDark900, BrandDefault500 , BlackDark900),
                modifier = Modifier
                    .width(150.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.confirm_button_text),
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}