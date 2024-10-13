package com.project17.tourbooking.activities.admin.destination

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Destination
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import kotlinx.coroutines.launch

@Composable
fun ManageDestinationScreen(navController: NavController) {
    val destinations = remember { mutableStateListOf<Destination>() }
    var isEditing by remember { mutableStateOf(false) }
    var editingDestinationId by remember { mutableStateOf<String?>(null) }
    var editingDestination by remember { mutableStateOf<Destination?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var destinationToDelete by remember{ mutableStateOf<Destination?>(null) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember{ mutableStateOf(false) }
    var isAbleToBack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        isAbleToBack = true
        destinations.addAll(FirestoreHelper.getAllDestination())
        isLoading = false
    }

    if (isDeleteDialogVisible && destinationToDelete != null) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = {
                isDeleteDialogVisible = false
                destinationToDelete = null
            },
            onConfirm = {
                isDeleteDialogVisible = false
                isLoading = true
                isAbleToBack = false
                coroutineScope.launch {
                    FirestoreHelper.deleteDestinationByDestinationId(destinationToDelete!!.id)
                    destinations.clear()
                    destinations.addAll(FirestoreHelper.getAllDestination())
                    isLoading = false
                    isAbleToBack = !isAbleToBack
                }
            },
            title = R.string.delete_destination_question_text,
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

        Text(text = stringResource(id = R.string.manage_destination_text), style = Typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        if (isEditing) {
            EditingDestinationForm(
                destination = editingDestination,
                isLoading = isLoading,
                onSave = { location, description ->
                    coroutineScope.launch {
                        isLoading = true
                        isAbleToBack = false
                        editingDestination = Destination(id = "", location = location, description = description)
                        isEditing = false
                        editingDestinationId?.let { FirestoreHelper.updateDestinationByDestinationId(it,
                            editingDestination!!
                        ) }
                        destinations.clear()
                        destinations.addAll(FirestoreHelper.getAllDestination())
                        Toast.makeText(context, context.getString(R.string.all_change_saved_text), Toast.LENGTH_SHORT).show()
                        isLoading = false
                        isAbleToBack = true
                    }
                },
                onCancel = {
                    isEditing = false
                    editingDestinationId = null
                    editingDestination = null
                }
            )
        } else {
            AddNewDestinationForm(isLoading) { location, description ->
                coroutineScope.launch {
                    isLoading = true
                    isAbleToBack = false

                    val destinationToAdd = Destination(id = "", location = location, description = description)

                    FirestoreHelper.createDestination(destinationToAdd)
                    destinations.clear()
                    destinations.addAll(FirestoreHelper.getAllDestination())

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
                items(destinations) { destination ->
                    DestinationItem(
                        destination = destination,
                        onEdit = {
                            isEditing = true
                            editingDestinationId = destination.id
                            editingDestination = destination
                        },
                        onDelete = {
                            destinationToDelete = destination
                            isDeleteDialogVisible = true
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun AddNewDestinationForm(isLoading: Boolean, onAddCategory: (String, String) -> Unit) {
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column {

        Text(text = stringResource(id = R.string.add_new_destination_text), style = Typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = location,
            onValueChange = { location = it },
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
        Button(onClick = {
            when{
                location.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.destination_location_empty_text), Toast.LENGTH_SHORT).show()
                }
                description.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.destination_description_empty_text), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    onAddCategory(location, description)
                    location = ""
                    description = ""
                }
            }
        }, enabled = !isLoading) {
            Text(stringResource(id = R.string.add_new_destination_text))
        }
    }
}

@Composable
fun EditingDestinationForm(
    destination: Destination?,
    isLoading: Boolean,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var location by remember { mutableStateOf(destination?.location ?: "") }
    var description by remember { mutableStateOf(destination?.description ?: "") }
    val context = LocalContext.current

    Column {
        Text(text = stringResource(id = R.string.edit_destination_text), style = Typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = location,
            readOnly = isLoading,
            onValueChange = { location = it },
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
        Row {
            Button(onClick = {
                when{
                    location.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.destination_location_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    description.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.destination_description_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        onSave(location, description)
                        location = ""
                        description = ""
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
fun DestinationItem(destination: Destination, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                Text(text = destination.location, style = Typography.bodyMedium)
                Text(text = destination.description, style = Typography.bodySmall)
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