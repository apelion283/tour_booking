package com.project17.tourbooking.navigates

import com.project17.tourbooking.R

sealed class NavigationItems(
    val route:String,
    val title: String,
    val icon: Int = 0,
    val iconFocused: Int = 0
) {
    //Client Items
    object Home: NavigationItems("home", "Home", R.drawable.ic_home, R.drawable.ic_home_focused)
    object MyTrip: NavigationItems("my_trip", "My Trip", R.drawable.ic_mytrip, R.drawable.ic_mytrip_focused)
    object WishList: NavigationItems("wish_list", "Wish List", R.drawable.ic_wishlist, R.drawable.ic_to_add_to_wishlist_3x)

    object Profile: NavigationItems("profile", "Profile", R.drawable.ic_profile, R.drawable.ic_profile_focused)
    object ChangePassword: NavigationItems("change_password", "Change Password")
    object PersonalInformation: NavigationItems("personal_information", "Personal Information")
    object Notification: NavigationItems("notification", "Notification")
    object FAQ: NavigationItems("faq", "FAQ")

    object TripDetail: NavigationItems("trip_detail", "Trip Detail")

    object Search: NavigationItems("search", "Search")
    object SearchFilter: NavigationItems("search_filter", "Search Filter")

    object BookingDetail: NavigationItems("booking_detail", "Booking Detail")
    object BookingSuccess: NavigationItems("booking_success", "Booking Success")
    object PaymentMethod: NavigationItems("payment_method", "Payment Method")

    object Login: NavigationItems("login", "Login")

    object Register: NavigationItems("create_account", "Register")
    object InputFullName: NavigationItems("input_full_name", "Input Full Name")
    object InputEmail: NavigationItems("input_email", "Input Email")
    object CreatePassword: NavigationItems("create_password", "Create Password")
    object UploadAvatar: NavigationItems("upload_avatar", "Upload Avatar")
    object AccountCreated: NavigationItems("account_created", "Account Created")

    object ForgotPassword: NavigationItems("forgot_password", "Forgot Password")

    object TripBookedDetail: NavigationItems("trip_booked_detail", "Trip Booked Detail")
    object CoinPackageBooking: NavigationItems("coin_package_booking", "Coin Package Booking")
    //Admin Items
    object ManageOverview : NavigationItems("manage_overview", "Manage Overview")
    object ManageTour: NavigationItems("manage_tours", "Manage Tour")
    object AddTour: NavigationItems("add_tour", "Add Tour")
    object EditTour: NavigationItems("edit_tour", "Edit Tour")
    object ManageAccount: NavigationItems("manage_accounts", "Manage Account")
    object ManageCategory: NavigationItems("manage_categories", "Manage Category")
    object ManageDestination: NavigationItems("manage_destinations", "Manage Destination")
    object Statistic: NavigationItems("statistic", "Statistic")
    object ManageCoinPackage: NavigationItems("manage_coin_package", "Manage Coin Package")
    object Explore: NavigationItems("explore", "Explore")
}