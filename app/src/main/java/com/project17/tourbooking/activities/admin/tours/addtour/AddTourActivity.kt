package com.project17.tourbooking.activities.admin.tours.addtour

import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firebase_cloud_helper.FirebaseCloudHelper.uploadImage
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonDatePickerDialog
import com.project17.tourbooking.utils.composables.EditCategoryOfTourInAdmin
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTourScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var description by remember { mutableStateOf("") }
    var slotQuantity by remember { mutableIntStateOf(0) }
    var image by remember { mutableStateOf("") }
    var openRegistrationDate by remember { mutableStateOf(Date()) }
    var closeRegistrationDate by remember { mutableStateOf(Date()) }
    var cancellationDeadline by remember { mutableStateOf(Date()) }
    var startDate by remember { mutableStateOf(Date()) }
    val (categories, setCategories) = remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesOfTour = remember {
        mutableStateListOf<Category>()
    }
    var destinationId by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activityResultRegistry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
    val getImage =
        activityResultRegistry?.register("key", ActivityResultContracts.GetContent()) { uri ->
            uri?.let { image = uri.toString() } }
    var moneyPrice by remember { mutableDoubleStateOf(0.0) }
    var coinPrice by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FirestoreHelper.getAllCategories { fetchedCategories ->
            setCategories(fetchedCategories)
        }

        destinations = FirestoreHelper.getAllDestination()
    }

    fun resetAllFields() {
        name = ""
        description = ""
        slotQuantity = 0
        image = ""
        openRegistrationDate = Date()
        closeRegistrationDate = Date()
        cancellationDeadline = Date()
        startDate = Date()
        destinationId = ""
        categoriesOfTour.clear()
        moneyPrice = 0.0
        coinPrice = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.add_new_tour_text),
            style = Typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.name_text)) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        var destinationExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = destinationExpanded,
            onExpandedChange = { destinationExpanded = !destinationExpanded }
        ) {
            TextField(
                value = destinations.find { it.id == destinationId }?.location ?: stringResource(
                    id = R.string.select_destination_text
                ),
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(id = R.string.destination_text)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationExpanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = destinationExpanded,
                onDismissRequest = { destinationExpanded = false }
            ) {
                destinations.forEach { destination ->
                    DropdownMenuItem(
                        text = { Text(destination.location) },
                        onClick = {
                            destinationId = destination.id
                            destinationExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(id = R.string.tour_description_text)) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = slotQuantity.toString(),
            onValueChange = { slotQuantity = it.toIntOrNull() ?: 0 },
            label = { Text(stringResource(id = R.string.quantity_slot_text)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { getImage?.launch("image/*") }) {
            Text(stringResource(id = R.string.pick_an_image_text))
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (image.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(image),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        CommonDatePickerDialog(
            title = stringResource(id = R.string.open_registration_date_text),
            date = openRegistrationDate,
            onDateChanged = { newDate -> openRegistrationDate = newDate }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CommonDatePickerDialog(
            title = stringResource(id = R.string.close_registration_date_text),
            date = closeRegistrationDate,
            onDateChanged = { newDate -> closeRegistrationDate = newDate }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CommonDatePickerDialog(
            title = stringResource(id = R.string.cancellation_deadline_text),
            date = cancellationDeadline,
            onDateChanged = { newDate -> cancellationDeadline = newDate }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CommonDatePickerDialog(
            title = stringResource(id = R.string.start_date_text),
            date = startDate,
            onDateChanged = { newDate -> startDate = newDate }
        )
        Spacer(modifier = Modifier.height(8.dp))

        var categoriesExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = categoriesExpanded,
            onExpandedChange = { categoriesExpanded = !categoriesExpanded }
        ) {
            TextField(
                value = stringResource(id = R.string.select_category_text),
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(id = R.string.category_text)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriesExpanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoriesExpanded,
                onDismissRequest = { categoriesExpanded = false }
            ) {
                categories
                    .filter { it !in categoriesOfTour }
                    .forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                categoriesOfTour.add(category)
                                categoriesExpanded = false
                            }
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(Modifier.fillMaxWidth()) {
            items(categoriesOfTour) { category ->
                EditCategoryOfTourInAdmin(
                    category,
                    onDeleteButtonClick = {
                        categoriesOfTour.remove(category)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = moneyPrice.toString(),
            onValueChange = { moneyPrice = it.toDoubleOrNull() ?: 0.0 },
            label = { Text(stringResource(id = R.string.money_price_text)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = coinPrice.toString(),
            onValueChange = { coinPrice = it.toIntOrNull() ?: 0 },
            label = { Text(stringResource(id = R.string.coin_price_text)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        var isLoading by remember {
            mutableStateOf(false)
        }
        Button(
            onClick = {
                when {
                    name.isBlank() -> {
                        Toast.makeText(context, context.getString(R.string.tour_name_empty_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    destinationId.isBlank() -> {
                        Toast.makeText(context, context.getString(R.string.destination_empty_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    description.isBlank() -> {
                        Toast.makeText(context, context.getString(R.string.tour_description_empty_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    slotQuantity <= 0 -> {
                        Toast.makeText(context, context.getString(R.string.slot_quantity_invalid_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    categoriesOfTour.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.categories_empty_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    image.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.image_empty_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    moneyPrice <= 0.0 -> {
                        Toast.makeText(context, context.getString(R.string.money_price_invalid_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coinPrice <= 0 -> {
                        Toast.makeText(context, context.getString(R.string.coin_price_invalid_text), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }

                coroutineScope.launch {
                    isLoading = true
                    val imageUrl = uploadImage(image, "tour_images")
                    val isSuccess = FirestoreHelper.createNewTour(
                        Tour(
                            id = "",
                            name = name,
                            description = description,
                            destinationId = destinationId,
                            image = imageUrl ?: "",
                            startDate = Timestamp(startDate),
                            openRegistrationDate = Timestamp(openRegistrationDate),
                            closeRegistrationDate = Timestamp(closeRegistrationDate),
                            cancellationDeadline = Timestamp(cancellationDeadline),
                            slotQuantity = slotQuantity,
                            averageRating = 0.0,
                            bookingCount = 0,
                        ),
                        Pair(moneyPrice, coinPrice),
                        categoriesOfTour
                    )

                    resetAllFields()

                    Toast.makeText(
                        context,
                        if (isSuccess) context.getString(R.string.add_new_tour_successfully_text)
                        else context.getString(R.string.something_went_wrong_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    isLoading = false
                }
            },
            enabled = !isLoading
        ) {
            Text(stringResource(id = R.string.add_new_tour_text))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
