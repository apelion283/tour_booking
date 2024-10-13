package com.project17.tourbooking.activities.user.trip_detail


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.project17.tourbooking.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Spacer
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Tour

@Composable
fun BillDetailScreen(
    billId: String,
    modifier: Modifier = Modifier
) {
    var tourImage by remember { mutableStateOf<String?>(null) }
    var tourName by remember { mutableStateOf<String?>(null) }
    var destination by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<String?>(null) }
    var ticketQuantity by remember { mutableStateOf(0) }
    var totalAmount by remember { mutableStateOf(0) }
    var bookingDate by remember { mutableStateOf<Date?>(null) }
    val tours = remember { mutableStateListOf<Tour>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(billId) {
        FirestoreHelper.getBillById(billId) { bill ->
            bill?.let {
                totalAmount = it.totalAmount.toInt()
                bookingDate = it.createdDate.toDate()
            }
        }

//        val loadedToursWithIds = FirestoreHelper.loadToursWithIds()
//        toursWithIds.clear()
//        toursWithIds.addAll(loadedToursWithIds)
//
//        FirestoreHelper.getBillDetailsByBillId(billId) { billDetails ->
//            if (billDetails.isNotEmpty()) {
//                val billDetail = billDetails.first()
//                ticketQuantity = billDetail.quantity
//
//                // Use a coroutine scope to handle async work
//                scope.launch {
//                    FirestoreHelper.getTourIdByTicketId(billDetail.ticketId) { tourId ->
//                        tourId?.let { id ->
//                            val tour = toursWithIds.find { tourWithId -> tourWithId.id == id }
//                            tour?.let { tourDetails ->
//                                tourImage = tourDetails.tour.image
//                                tourName = tourDetails.tour.name
//                                destination = tourDetails.tour.destination
//
//                                // Convert Timestamp to Date and format
//                                val date = tourDetails.tour.startDate.toDate()
//                                startDate = date?.let {
//                                    // Format the date as needed
//                                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    // UI content
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Your Trip",
            style = Typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tour Image
        tourImage?.let { imageUrl ->
            Image(
                painter = rememberImagePainter(data = imageUrl),
                contentDescription = "Tour Image",
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tour Name
        tourName?.let {
            Text(
                text = it,
                style = Typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Destination
        destination?.let {
            Text(
                text = "Destination: $it",
                style = Typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Start Date
        startDate?.let {
            Text(
                text = "Start Date: $it",
                style = Typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Number of Tickets
        Text(
            text = "Number of Tickets: $ticketQuantity",
            style = Typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Total Amount
        Text(
            text = "Total Amount: $$totalAmount",
            style = Typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Booking Date
        bookingDate?.let { date ->
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = formatter.format(date)
            Text(
                text = "Booking Date: $formattedDate",
                style = Typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
