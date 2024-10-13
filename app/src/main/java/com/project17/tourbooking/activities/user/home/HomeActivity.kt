package com.project17.tourbooking.activities.user.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.project17.tourbooking.R
import com.project17.tourbooking.constant.CATEGORY_GRID_COLUMN
import com.project17.tourbooking.constant.FILTER_RATE_FROM
import com.project17.tourbooking.constant.TOP_BOOKING_TOUR_LIMIT
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.composables.CategoryItem
import com.project17.tourbooking.utils.composables.CommonAlertDialog
import com.project17.tourbooking.utils.composables.TourCardInHorizontal
import com.project17.tourbooking.utils.composables.TourCardInVertical
import com.project17.tourbooking.viewmodels.AppViewModel
import com.project17.tourbooking.viewmodels.AuthState
import com.project17.tourbooking.viewmodels.AuthViewModel
import kotlin.system.exitProcess

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()
) {
    var isDialogVisible by remember {
        mutableStateOf(false)
    }
    BackHandler(
        onBack = {
            isDialogVisible = !isDialogVisible
        }
    )
    if (isDialogVisible) {
        CommonAlertDialog(
            isDialogVisible = true,
            onDismiss = { isDialogVisible = false },
            onConfirm = {
                exitProcess(0)
            },
            title = R.string.exit_app_alert_dialog_title_text,
            message = R.string.exit_app_alert_dialog_message_text,
            confirmButtonText = R.string.confirm_button_text,
            dismissButtonText = R.string.cancel_button_text
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                top = 24.dp,
                start = 16.dp,
                end = 16.dp
            )
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(navController = navController, authViewModel = authViewModel)
        Spacer(modifier = Modifier.height(16.dp))
        SearchBarSection(navController)
        Spacer(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
        )
        ChooseCategorySection(navController = navController, appViewModel = appViewModel)
        Spacer(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
        )
        PopularTourSection(navController = navController)
        Spacer(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
        )
        MostBookedSection(navController = navController)
    }
}

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    hasUnreadNotification: Boolean = false,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    var userName by remember { mutableStateOf<String?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = authViewModel.getCurrentUser()

                FirestoreHelper.getAvatarUrlFromAccountId(currentUser!!.uid) { url ->
                    avatarUrl = url
                }
                FirestoreHelper.getUserNameByAccountId(currentUser.uid) { userName = it }

            }
            is AuthState.Error -> {}
            is AuthState.Unauthenticated -> {}
            else -> Unit
        }
    }


    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable { navController.navigate(NavigationItems.Profile.route) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = avatarUrl ?: R.drawable.default_avatar,
                contentDescription = stringResource(id = R.string.avatar_description_text),
                placeholder = painterResource(id = R.drawable.default_avatar),
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = {
                        navController.navigate(NavigationItems.Profile.route)
                    })
                    .border(1.dp, BlackLight300, RoundedCornerShape(50.dp))
            )

            Text(
                text = "Hi, ${userName ?: stringResource(id = R.string.default_username)}!",
                modifier = Modifier.padding(start = 10.dp),
                style = Typography.titleMedium
            )
        }

        if (hasUnreadNotification) {
            BadgedBox(
                badge = {
                    Badge()
                },
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = stringResource(id = R.string.icon_notification_description_text),
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate(NavigationItems.Notification.route)
                    })
                        .size(30.dp)
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = stringResource(id = R.string.icon_notification_description_text),
                modifier = Modifier.clickable(onClick = {
                    navController.navigate(NavigationItems.Notification.route)
                }).size(30.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(id = R.string.home_question_text),
        modifier = Modifier.fillMaxWidth(),
        style = Typography.headlineMedium,
        maxLines = 2,
        overflow = TextOverflow.Clip
    )
}


@Composable
fun SearchBarSection(navController: NavHostController) {
    TextField(
        value = "",
        onValueChange = {
        },
        interactionSource = remember {
            MutableInteractionSource()
        }.also { source ->
            LaunchedEffect(source) {
                source.interactions.collect {
                    if (it is PressInteraction.Release) {
                        navController.navigate(NavigationItems.Search.route)
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search_icon_description_text),
                tint = BlackDark900
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_hint_text),
                style = Typography.bodyMedium,
                color = BlackLight300
            )
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = BlackLight100,
            focusedContainerColor = BlackLight100,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun ChooseCategorySection(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    LaunchedEffect(Unit) {
        appViewModel.loadCategories(FirestoreHelper)
    }

    var isSeeAllClicked by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.choose_category_text),
                style = Typography.headlineMedium
            )
            Text(
                text = if (!isSeeAllClicked) stringResource(id = R.string.see_all_text)
                else stringResource(id = R.string.collapse_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable { isSeeAllClicked = !isSeeAllClicked }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (!isSeeAllClicked) {
            LazyRow {
                items(appViewModel.categories) { category ->
                    CategoryItem(category = category, onClick = {
                        navController.navigate(route = NavigationItems.Search.route + "/${category.id}")
                    })
                }
            }
        } else {
            CategoryGrid(
                items = appViewModel.categories,
                columns = CATEGORY_GRID_COLUMN,
                navController = navController,
                appViewModel = appViewModel
            )
        }
    }
}

@Composable
fun CategoryGrid(
    items: List<Category>,
    columns: Int,
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val itemWeight = 1f / columns
                rowItems.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .weight(itemWeight)
                    ) {
                        CategoryItem(
                            category = item,
                            isSelected = appViewModel.selectedCategory.value == item,
                            onClick = {
                                navController.navigate(route = NavigationItems.Search.route + "/${item.id}")
                            }
                        )
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(itemWeight))
                }
            }
        }
    }
}


@Composable
fun PopularTourSection(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val tours = remember { mutableStateListOf<Tour>() }

    LaunchedEffect(Unit) {
        val highRatedTours = FirestoreHelper.getToursFromRating(FILTER_RATE_FROM)
        tours.addAll(highRatedTours)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.popular_tour_text),
                style = Typography.headlineMedium
            )
            Text(
                text = stringResource(id = R.string.explore_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable(onClick = {
                    navController.navigate(NavigationItems.WishList.route)
                })
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(if(tours.isNotEmpty()) 250.dp else 0.dp)
        ) {
            items(tours) { tour ->
                TourCardInVertical(tour = tour, navController = navController)
            }
        }
    }
}


@Composable
fun MostBookedSection(modifier: Modifier = Modifier, navController: NavHostController) {
    var packages by remember {
        mutableStateOf<List<Tour>>(emptyList())
    }
    var isSeeAllClicked by remember {
        mutableStateOf(false)
    }
    var lazyColumnHeight by remember {
        mutableStateOf(0.dp)
    }

    LaunchedEffect(Unit) {
        packages = FirestoreHelper.getAllTours()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.all_tour_text),
                style = Typography.headlineMedium
            )
            Text(
                text = if (!isSeeAllClicked) stringResource(id = R.string.see_all_text)
                else stringResource(id = R.string.collapse_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable(onClick = {
                    isSeeAllClicked = !isSeeAllClicked
                })
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (!isSeeAllClicked) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lazyColumnHeight)
            ) {
                items(packages) { item ->
                    TourCardInHorizontal(
                        tour = item,
                        navController = navController,
                        onMeasured = {
                            lazyColumnHeight = it
                        }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                packages.forEach { item ->

                    TourCardInHorizontal(
                        tour = item,
                        navController = navController,
                        onMeasured = {
                            lazyColumnHeight = it
                        })
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(40.dp))
}

@Composable
@Preview
fun HomePreview() {
    TourBookingTheme {
        HomeScreen()
    }
}