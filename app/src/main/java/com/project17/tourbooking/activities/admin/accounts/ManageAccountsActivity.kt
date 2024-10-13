
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Account
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ManageAccountsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var isAbleToBack by remember {
        mutableStateOf(true)
    }
    var isLoading by remember {
        mutableStateOf(false)
    }
    var isDeleteDialogVisible by remember {
        mutableStateOf(false)
    }

    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.SignUpSuccess -> {
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        accounts = FirestoreHelper.getAllAccounts()
    }

    if (isDeleteDialogVisible && selectedAccount != null) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = {
                isDeleteDialogVisible = false
                selectedAccount = null
            },
            onConfirm = {
                isDeleteDialogVisible = false
                isLoading = true
                isAbleToBack = false
                scope.launch {
                    FirestoreHelper.deleteAccountByAccountId(selectedAccount!!.id)
                    accounts = FirestoreHelper.getAllAccounts()
                    isLoading = false
                    isAbleToBack = !isAbleToBack
                }
            },
            title = R.string.delete_text,
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

        Button(onClick = { /*TODO*/ }) {
            Text(text = stringResource(id = R.string.add_new_account_text))
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(id = R.string.manage_account_text), style = Typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            if(!isLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(accounts) { account ->
                        AccountItem(
                            account = account,
                            onEdit = {
                                selectedAccount = account
                                navController.navigate("edit_account")
                            },
                            onDelete = {
                                selectedAccount = account
                                isDeleteDialogVisible = true
                            }
                        )
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BlackLight300))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            else CircularProgressIndicator()
        }
    }
}


@Composable
fun AccountItem(account: Account, onEdit: (Account) -> Unit, onDelete: (Account) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
        .clickable { onEdit(account) }) {
        Image(painter = rememberAsyncImagePainter(
            model = account.avatar,
            contentScale = ContentScale.Crop
        ), contentDescription = "Avatar", modifier = Modifier
            .clip(CircleShape)
            .size(50.dp)
            .border(1.dp, BlackLight300, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Text("Username: ${account.userName}", style = Typography.titleLarge)
            Text("Role: ${account.role}", )
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
                    text = String.format("%d", account.coin),
                    color = ErrorDefault500
                )
            }

        }
        TextButton(onClick = { onDelete(account) }) {
            Text(stringResource(id = R.string.delete_text))
        }
    }
}

