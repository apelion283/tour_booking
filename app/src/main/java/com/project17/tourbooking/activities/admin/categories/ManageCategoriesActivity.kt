package com.project17.tourbooking.activities.admin.categories

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firebase_cloud_helper.FirebaseCloudHelper
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.ui.theme.BlackLight300
import kotlinx.coroutines.launch
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog

@Composable
fun ManageCategoriesScreen(navController: NavController) {
    var (categories, setCategories) = remember { mutableStateOf<List<Category>>(emptyList()) }
    var isEditing by remember { mutableStateOf(false) }
    var editingCategoryId by remember { mutableStateOf<String?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var categoryToDelete by remember{ mutableStateOf<Category?>(null) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isLoading by remember{ mutableStateOf(false) }
    var isAbleToBack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        isAbleToBack = true
        FirestoreHelper.getAllCategories {
            fetchedCategories -> setCategories(fetchedCategories)
        }
        isLoading = false
    }

    if (isDeleteDialogVisible && categoryToDelete != null) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = {
                isDeleteDialogVisible = false
                categoryToDelete = null
            },
            onConfirm = {
                isDeleteDialogVisible = false
                isLoading = true
                isAbleToBack = false
                coroutineScope.launch {
                    FirestoreHelper.deleteCategoryByCategoryId(categoryToDelete!!.id)
                    FirestoreHelper.getAllCategories { fetchedCategories ->
                        setCategories(fetchedCategories)
                    }
                    isLoading = false
                    isAbleToBack = !isAbleToBack
                }
            },
            title = R.string.delete_category_question_text,
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

        Text(text = stringResource(id = R.string.manage_category_text), style = Typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        if (isEditing) {
            EditingCategoryForm(
                category = editingCategory,
                isLoading = isLoading,
                onSave = { name, image, imageUpdateUri ->
                    coroutineScope.launch {
                        isLoading = true
                        isAbleToBack = false
                        val imageUrl = FirebaseCloudHelper.updateImage(image, imageUpdateUri, "category_images")
                        editingCategory = Category(id = "", name = name, image = imageUrl)
                        isEditing = false
                        editingCategoryId?.let { FirestoreHelper.updateCategoryByCategoryId(it,
                            editingCategory!!
                        ) }
                        FirestoreHelper.getAllCategories { fetchedCategories ->
                            setCategories(fetchedCategories)
                        }
                        Toast.makeText(context, context.getString(R.string.all_change_saved_text), Toast.LENGTH_SHORT).show()
                        isLoading = false
                        isAbleToBack = true
                    }
                },
                onCancel = {
                    isEditing = false
                    editingCategoryId = null
                    editingCategory = null
                }
            )
        } else {
            AddNewCategoryForm(isLoading) {name, imageUri ->
                coroutineScope.launch {
                    isLoading = true
                    isAbleToBack = false

                    val categoryToAdd = Category(id = "", name = name, image = imageUri)

                    FirestoreHelper.createCategory(categoryToAdd)
                    FirestoreHelper.getAllCategories { fetchedCategories ->
                        setCategories(fetchedCategories)
                    }

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
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        onEdit = {
                            isEditing = true
                            editingCategoryId = category.id
                            editingCategory = category
                        },
                        onDelete = {
                            categoryToDelete = category
                            isDeleteDialogVisible = true
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun AddNewCategoryForm(isLoading: Boolean, onAddCategory: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it.toString()
    }

    Column {

        Text(text = stringResource(id = R.string.add_new_category_text), style = Typography.titleLarge)
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
        Button(onClick = { launcher.launch("image/*") }, enabled = !isLoading) {
            Text(stringResource(id = R.string.pick_an_image_text))
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (imageUri.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected Icon",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, BlackLight300)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            when{
                name.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.category_name_empty_text), Toast.LENGTH_SHORT).show()
                }
                imageUri.isEmpty() -> {
                    Toast.makeText(context, context.getString(R.string.image_empty_text), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    onAddCategory(name, imageUri)
                    name = ""
                    imageUri = ""
                }
            }
        }, enabled = !isLoading) {
            Text(stringResource(id = R.string.add_new_category_text))
        }
    }
}

@Composable
fun EditingCategoryForm(
    category: Category?,
    isLoading: Boolean,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var image by remember { mutableStateOf(category?.image ?: "") }
    var imageUpdateUri by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUpdateUri = it.toString()
    }

    Column {
        Text(text = stringResource(id = R.string.edit_category_text), style = Typography.titleLarge)
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
        Button(onClick = { launcher.launch("image/*") }, enabled = !isLoading) {
            Text(stringResource(id = R.string.pick_an_image_text))
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (image.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(if(imageUpdateUri.isNullOrEmpty()) image else imageUpdateUri),
                contentDescription = "Selected Category Icon",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, BlackLight300)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                when{
                    name.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.category_name_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    image.isEmpty() -> {
                        Toast.makeText(context, context.getString(R.string.image_empty_text), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        onSave(name, image, imageUpdateUri)
                        name = ""
                        image = ""
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
fun CategoryItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            if (category.image.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(category.image),
                    contentDescription = "Category Icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, BlackLight300)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text(text = category.name, style = Typography.bodyMedium)
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
