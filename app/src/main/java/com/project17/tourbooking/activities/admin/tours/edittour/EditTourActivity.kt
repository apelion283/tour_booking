package com.project17.tourbooking.activities.admin.tours.edittour

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.project17.tourbooking.helper.firebase_cloud_helper.FirebaseCloudHelper
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.utils.composables.EditCategoryOfTourInAdmin
import com.project17.tourbooking.utils.composables.CommonDatePickerDialog
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTourScreen(tourId: String, navController: NavController) {
    var tour by remember { mutableStateOf<Tour?>(null) }
    var name by remember { mutableStateOf("") }
    var destinations by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var slotQuantity by remember { mutableIntStateOf(0) }
    var image by remember { mutableStateOf("") }
    var openRegistrationDate by remember { mutableStateOf(Date()) }
    var closeRegistrationDate by remember { mutableStateOf(Date()) }
    var cancellationDeadline by remember { mutableStateOf(Date()) }
    var startDate by remember { mutableStateOf(Date()) }
    var averageRating by remember { mutableDoubleStateOf(0.0) }
    val (categories, setCategories) = remember { mutableStateOf<List<Category>>(emptyList()) }
    var categoriesOfTour = remember { mutableStateListOf<Category>() }
    var destinationId by remember { mutableStateOf("") }
    var tourDescription by remember { mutableStateOf("") }
    var moneyPrice by remember { mutableDoubleStateOf(0.0) }
    var coinPrice by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var isBackable by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val activityResultRegistry = LocalActivityResultRegistryOwner.current?.activityResultRegistry
    val getImage =
        activityResultRegistry?.register("key1", ActivityResultContracts.GetContent()) { uri ->
            uri?.let { image = uri.toString() }
        }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tourId) {
        isLoading = true
        tour = FirestoreHelper.getTourByTourId(tourId)
        FirestoreHelper.getAllCategories { fetchedCategories ->
            setCategories(fetchedCategories)
        }

        destinations = FirestoreHelper.getAllDestination()

        tour?.let {
            name = it.name
            slotQuantity = it.slotQuantity
            image = it.image
            image = image
            tourDescription = it.description
            destinationId = it.destinationId
            openRegistrationDate = it.openRegistrationDate.toDate()
            closeRegistrationDate = it.closeRegistrationDate.toDate()
            cancellationDeadline = it.cancellationDeadline.toDate()
            startDate = it.startDate.toDate()
            averageRating = it.averageRating
        }

        categoriesOfTour.addAll(FirestoreHelper.getCategoriesOfTourByTourId(tourId))

        val price = FirestoreHelper.getTicketPriceByTourId(tourId)
        moneyPrice = price.first.toDouble()
        coinPrice = price.second.toInt()
        isLoading = false
    }

    if (!isBackable) {
        BackHandler{
        }
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
                    .clickable {
                        if (isBackable) navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.edit_tour_text),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if(isLoading) CircularProgressIndicator()
        else{

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
                value = tourDescription,
                onValueChange = { tourDescription = it },
                label = { Text(stringResource(id = R.string.tour_description_text)) }
            )

            Spacer(Modifier.height(8.dp))

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    Toast.makeText(context, context.getString(R.string.all_change_discarded_text), Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }) {
                    Text(stringResource(id = R.string.cancel_button_text))
                }

                Button(onClick = {
                    when {
                        name.isBlank() -> Toast.makeText(
                            context,
                            R.string.tour_name_empty_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        destinationId.isBlank() -> Toast.makeText(
                            context,
                            R.string.destination_empty_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        tourDescription.isBlank() -> Toast.makeText(
                            context,
                            R.string.tour_description_empty_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        slotQuantity <= 0 || slotQuantity.toString().isEmpty() -> Toast.makeText(
                            context,
                            R.string.slot_quantity_invalid_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        categoriesOfTour.isEmpty() -> Toast.makeText(
                            context,
                            R.string.categories_empty_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        image.isEmpty() -> Toast.makeText(
                            context,
                            R.string.image_empty_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        moneyPrice <= 0.0 -> Toast.makeText(
                            context,
                            R.string.money_price_invalid_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        coinPrice <= 0 -> Toast.makeText(
                            context,
                            R.string.coin_price_invalid_text,
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> {
                            isBackable = false
                            isLoading = true
                            coroutineScope.launch {
                                image = FirebaseCloudHelper.updateImage(tour?.image!!, image, "tour_images")
                                FirestoreHelper.updateTour(
                                    tourId,
                                    Tour(
                                        id = tourId,
                                        name = name,
                                        openRegistrationDate = Timestamp(openRegistrationDate),
                                        closeRegistrationDate = Timestamp(closeRegistrationDate),
                                        cancellationDeadline = Timestamp(cancellationDeadline),
                                        startDate = Timestamp(startDate),
                                        slotQuantity = slotQuantity,
                                        image = image,
                                        bookingCount = tour?.bookingCount!!,
                                        averageRating = tour?.averageRating!!,
                                        description = tourDescription,
                                        destinationId = destinationId
                                    ),
                                    Pair(moneyPrice, coinPrice),
                                    categoriesOfTour
                                )
                                Toast.makeText(context, context.getString(R.string.all_change_saved_text), Toast.LENGTH_SHORT).show()
                                isBackable = true
                                isLoading = false
                                navController.popBackStack()
                            }
                        }
                    }
                }) {
                    Text(stringResource(id = R.string.save_text))
                }
            }
        }
    }
}

