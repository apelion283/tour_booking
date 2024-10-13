import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.project17.tourbooking.activities.user.search.presentation.search.SearchBarSection
import com.project17.tourbooking.activities.user.search.viewmodel.SearchViewModel
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.viewmodels.AuthViewModel
import com.project17.tourbooking.utils.composables.RequireLogin
import com.project17.tourbooking.viewmodels.AuthState

@Composable
fun WishListScreen(searchViewModel: SearchViewModel = viewModel(), navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val wishlists = remember { mutableStateListOf<Tour>() }

    if (authState.value is AuthState.Unauthenticated) {
        RequireLogin(navController)
    } else {

        LaunchedEffect(Unit) {
            val accountId = authViewModel.getCurrentUser()?.uid
            if(!accountId.isNullOrEmpty()){
//                val wishlistItemIdList = FirestoreHelper.getTourIdListOfWishListByAccountId(accountId)
//                wishlistItemIdList
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your Wish List",
                style = Typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBarSection(searchViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wishlists) { item ->
//                    WishlistItem(item, onRemoveFromWishlist = { /* Xử lý bỏ yêu thích */ }) {
//                        navController.navigate("login")
//                    }
                }
            }
        }
    }
}

//@Composable
//fun WishlistItem(item: WishlistItem, onRemoveFromWishlist: (Int) -> Unit, onClick: () -> Unit) {
//    var isFavorite by remember { mutableStateOf(true) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//            .clickable { onClick() },
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        border = BorderStroke(1.dp, BlackLight300)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Image(
//                painter = rememberAsyncImagePainter(
//                    ImageRequest.Builder(LocalContext.current).data(data = item.imageUrl)
//                        .apply(block = fun ImageRequest.Builder.() {
//                            crossfade(true)
//                            placeholder(R.drawable.kuta_resort)
//                            error(R.drawable.kuta_resort)
//                        }).build()
//                ),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxHeight()
//                .width(80.dp)
//                .clip(RoundedCornerShape(8.dp)),
//            contentScale = ContentScale.Crop
//            )
//            Spacer(modifier = Modifier.width(16.dp))
//            Column(modifier = Modifier.weight(1f)) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = item.name, style = Typography.titleLarge)
//                    Spacer(Modifier.weight(1f))
//                    IconButton(onClick = {
//                        isFavorite = !isFavorite
//                        if (!isFavorite) {
//                            onRemoveFromWishlist(item.id)
//                        }
//                    }) {
//                        Icon(
//                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
//                            contentDescription = if (isFavorite) "Remove from wishlist" else "Add to wishlist",
//                            tint = if (isFavorite) Color.Red else Color.Gray
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(text = item.price, style = Typography.bodyLarge, color = Color(0xFFFFA500))
//                Spacer(modifier = Modifier.height(8.dp))
//                Row {
//                    for (i in 1..5) {
//                        Icon(
//                            painter = painterResource(id = if (i <= item.rating.toInt()) R.drawable.ic_yellow_star else R.drawable.ic_white_star),
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = if (i <= item.rating.toInt()) Color.Yellow else Color.Gray
//                        )
//                    }
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(text = item.rating.toString(), style = Typography.bodyMedium)
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(text = item.description, style = Typography.bodySmall, color = BlackLight300)
//            }
//        }
//    }
//}