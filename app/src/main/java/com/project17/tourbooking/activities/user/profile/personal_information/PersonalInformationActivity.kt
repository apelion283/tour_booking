package com.project17.tourbooking.activities.user.profile.personal_information

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.DEFAULT_AVATAR
import com.project17.tourbooking.helper.firebase_cloud_helper.FirebaseCloudHelper
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Account
import com.project17.tourbooking.models.Customer
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDark900
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CustomOutlinedTextField
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PersonalInformationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PersonalInformationScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
) {
    var fullName by remember { mutableStateOf("") }
    var isFullNameError by remember {
        mutableStateOf(false)
    }
    var fullNameErrorMessage by remember {
        mutableStateOf("")
    }

    var userName by remember { mutableStateOf("") }
    var isUserNameError by remember {
        mutableStateOf(false)
    }
    var userNameErrorMessage by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember { mutableStateOf("") }
    var isPhoneNumberError by remember {
        mutableStateOf(false)
    }
    var phoneNumberErrorMessage by remember {
        mutableStateOf("")
    }
    val phoneNumberInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }

    var address by remember { mutableStateOf("") }
    var isAddressError by remember {
        mutableStateOf(false)
    }
    var addressErrorMessage by remember {
        mutableStateOf("")
    }

    var selectedGender by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var dateOfBirth by remember { mutableStateOf<Date?>(null) }

    var selectedImageUri by remember { mutableStateOf("") }

    var account by remember { mutableStateOf<Account?> (null) }
    var customer by remember { mutableStateOf<Customer?> (null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri.toString()
        }
    )

    var isLoading by remember {  mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                isLoading = true
                account = authViewModel.getCurrentUser()?.uid?.let {
                    FirestoreHelper.getAccountByAccountId(
                        it
                    )
                }
                account?.let { it ->
                    FirestoreHelper.getCustomerInfoByCustomerId(it.customerId){
                        customer = it
                        fullName = customer?.fullName ?: ""
                        selectedGender = customer?.gender ?: true
                        address = customer?.address ?: ""
                        phoneNumber = customer?.phoneNumber ?: ""
                        userName = account?.userName ?: ""
                        dateOfBirth = customer?.dateOfBirth?.toDate() ?: Date()
                    }
                }
                isLoading = false
            }

            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState.value as AuthState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(navController = navController, Modifier)

        Spacer(modifier = Modifier.height(16.dp))

        if(isLoading){
            CircularProgressIndicator(color = BrandDefault500)
        }
        else {
            Text(
                text = stringResource(id = R.string.update_your_profile_information_text),
                style = Typography.headlineMedium,
                color = BlackLight300,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.select_avatar_text),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = BlackDark900,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedImageUri.isNotEmpty()) {
                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri.toUri())
                    } else {
                        val source =
                            ImageDecoder.createSource(context.contentResolver, selectedImageUri.toUri())
                        ImageDecoder.decodeBitmap(source)
                    }

                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected Avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(1.dp, BlackLight300, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(model = account?.avatar ?: DEFAULT_AVATAR),
                        contentDescription = "Default Avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(1.dp, BlackLight300, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(start = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandDefault500,
                        contentColor = BlackDark900
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.pick_avatar_text),
                        style = Typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    isFullNameError = false
                    fullNameErrorMessage = ""
                },
                label = stringResource(id = R.string.full_name_text),
                interactionSource = remember { MutableInteractionSource() },
                isError = isFullNameError,
                errorMessage = fullNameErrorMessage
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.date_of_birth_text) + ":",
                style = Typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDatePicker(context, initDate = dateOfBirth){ dateOfBirth = it } },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDefault500,
                    contentColor = BlackDark900
                )
            ) {
                Text(formatDate( dateOfBirth, context), Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.gender_text) + ":",
                style = Typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = selectedGender,
                    onClick = { selectedGender = true },
                    colors = RadioButtonDefaults.colors(selectedColor = BrandDefault500)
                )
                Text(stringResource(id = R.string.male_text), style = Typography.bodyLarge)

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = !selectedGender,
                    onClick = { selectedGender = false },
                    colors = RadioButtonDefaults.colors(selectedColor = BrandDefault500)
                )
                Text(stringResource(id = R.string.female_text), style = Typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    isAddressError = false
                    addressErrorMessage = ""
                },
                label = stringResource(id = R.string.address_text),
                interactionSource = remember { MutableInteractionSource() },
                isError = isAddressError,
                errorMessage = addressErrorMessage
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {newValue ->
                    if(newValue.length <= 11){
                        phoneNumber = newValue
                    }
                    isPhoneNumberError = false
                    phoneNumberErrorMessage = ""
                },
                label = {
                    Text(
                        stringResource(id = R.string.phone_number_text),
                        style = Typography.titleMedium,
                        color = BlackLight300
                    )
                },
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                interactionSource = phoneNumberInteractionSource,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (phoneNumberInteractionSource.collectIsFocusedAsState().value) BrandDefault500 else BlackLight300,
                    unfocusedBorderColor = BlackLight300,
                    focusedLabelColor = if (phoneNumberInteractionSource.collectIsFocusedAsState().value) BrandDefault500 else BlackLight300,
                    unfocusedLabelColor = BlackLight300,
                    cursorColor = BrandDefault500
                ),
                singleLine = true,
                isError = isPhoneNumberError,
                supportingText = {
                    if (isPhoneNumberError) {
                        Text(
                            text = phoneNumberErrorMessage,
                            style = Typography.bodySmall,
                            color = ErrorDark900
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Phone),
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomOutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    isUserNameError = false
                    userNameErrorMessage = ""
                },
                label = stringResource(id = R.string.user_name_text),
                interactionSource = remember { MutableInteractionSource() },
                isError = isUserNameError,
                errorMessage = userNameErrorMessage,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if(fullName.isEmpty()){
                        isFullNameError = true
                        fullNameErrorMessage = context.getString(R.string.this_field_is_required_text)
                    }
                    if(userName.isEmpty()){
                        isUserNameError = true
                        userNameErrorMessage = context.getString(R.string.this_field_is_required_text)
                    }
                    if(phoneNumber.length < 10 && phoneNumber.isNotEmpty()){
                        isPhoneNumberError = true
                        phoneNumberErrorMessage = context.getString(R.string.invalid_phone_number_text)
                    }
                    if(!isPhoneNumberError && !isUserNameError && !isFullNameError) {
                        scope.launch {
                            val isUserNameExist = FirestoreHelper.isUsernameExists(userName)

                            if (userName != account!!.userName && isUserNameExist) {
                                isUserNameError = true
                                userNameErrorMessage = context.getString(R.string.username_already_exist_text)
                                return@launch
                            }

                            isLoading = true

                            val accountAvatarUrl = FirebaseCloudHelper.updateImage(
                                account!!.avatar,
                                selectedImageUri,
                                "avatars"
                            )

                            account?.let {
                                FirestoreHelper.updateAccountByAccountId(
                                    it.id,
                                    it.copy(
                                        userName = userName,
                                        avatar = accountAvatarUrl
                                    )
                                )
                            }

                            val customerDateOfBirth = dateOfBirth?.let { Timestamp(it) } ?: customer!!.dateOfBirth
                            FirestoreHelper.updateCustomer(
                                customer!!.id,
                                customer!!.copy(
                                    fullName = fullName,
                                    address = address,
                                    gender = selectedGender,
                                    phoneNumber = phoneNumber,
                                    dateOfBirth = customerDateOfBirth
                                )
                            )

                            isLoading = false
                            Toast.makeText(context, context.getString(R.string.update_information_success_text), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDefault500,
                    contentColor = BlackDark900
                )
            ) {
                Text(
                    stringResource(id = R.string.save_text),
                    style = Typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderSection(navController: NavController, modifier: Modifier) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = modifier
                .padding(start = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Back"
            )
        }
        Text(
            text = stringResource(id = R.string.personal_information_text),
            style = Typography.headlineSmall,
            modifier = modifier
                .padding(end = 16.dp))
        Spacer(modifier = Modifier.width(1.dp))
    }
}

private fun formatDate(date: Date?, context: Context): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return date?.let { format.format(it) }
        ?: context.getString(R.string.select_date_of_birth_text)
}

fun showDatePicker(context: Context, initDate: Date?, callback: (Date) -> Unit) {
    val calendar = Calendar.getInstance().apply {
        initDate?.let { time = it }
    }
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dateOfBirth = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
            callback(dateOfBirth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis
    datePickerDialog.show()
}

@Preview
@Composable
fun PersonalInformationScreenPreview() {
    TourBookingTheme {
        PersonalInformationScreen()
    }
}