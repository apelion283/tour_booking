package com.project17.tourbooking.activities.admin.tours

import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.models.Tour
import kotlinx.coroutines.launch
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackLight200
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog

@Composable
fun ManageToursScreen(navController: NavController) {
    var tours by remember { mutableStateOf<List<Tour>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var tourToDelete by remember { mutableStateOf<Tour?>(null) }
    val context = LocalContext.current
    var isLoading by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        isLoading = false
        val fetchedTours = FirestoreHelper.getAllTours()
        tours = fetchedTours
        isLoading = true
    }

    if (isDeleteDialogVisible && tourToDelete != null) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = {
                isDeleteDialogVisible = false
                tourToDelete = null
            },
            onConfirm = {
                isDeleteDialogVisible = false
                isLoading = true
                coroutineScope.launch {
                    FirestoreHelper.deleteTourByTourId(tourToDelete!!.id)

                    tours = tours.filter { it.id != tourToDelete!!.id }
                    isDeleteDialogVisible = false
                    tourToDelete = null
                    Toast.makeText(context, context.getString(R.string.delete_successfully_text), Toast.LENGTH_SHORT).show()
                }
                isLoading = !isLoading
            },
            title = R.string.delete_text,
            message = R.string.are_your_sure_text,
            confirmButtonText = R.string.confirm_button_text,
            dismissButtonText = R.string.cancel_button_text
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
        if(isLoading){
            Button(onClick = {
                navController.navigate(NavigationItems.AddTour.route)
            }) {
                Text(stringResource(id = R.string.add_new_tour_text))
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tours) { tour ->
                    TourItem(
                        tour = tour,
                        onEdit = {
                            navController.navigate(NavigationItems.EditTour.route + "/${tour.id}")
                        },
                        onDelete = {
                            tourToDelete = tour
                            isDeleteDialogVisible = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }else CircularProgressIndicator()
    }
}


@Composable
fun TourItem(
    tour: Tour,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(tour) {
        categories = FirestoreHelper.getCategoriesOfTourByTourId(tour.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = {
                onEdit()
            })
            .border(
                width = 1.dp,
                color = BlackLight200,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackWhite0)
                .padding(8.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(tour.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(200.dp)
                    .width(150.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = tour.name, style = Typography.headlineSmall)
                Text(
                    text = stringResource(id = R.string.category_text) + ": " + categories.joinToString(", ") { it.name },
                    style = Typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(stringResource(id = R.string.quantity_slot_text) + " ${tour.slotQuantity}")
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(id = R.string.remaining_slot_text) + " ${tour.slotQuantity - tour.bookingCount}")
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Text(stringResource(id = R.string.delete_text))
                    }
                }
            }
        }
    }
}

