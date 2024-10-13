package com.project17.tourbooking.models

import com.google.firebase.Timestamp
import com.project17.tourbooking.constant.ACCOUNT_ROLE
import com.project17.tourbooking.constant.ACCOUNT_STATUS
import com.project17.tourbooking.constant.COUPON_STATUS
import com.project17.tourbooking.constant.PAYMENT_STATUS
import com.project17.tourbooking.constant.TICKET_TYPE
import com.project17.tourbooking.constant.VALUE_TYPE

data class Category(
    val id: String,
    val name: String,
    val image: String
) {
    constructor() : this("", "", "")
}

data class Destination(
    val id: String,
    var location: String,
    val description: String
){
    constructor(): this("", "", "")
}

data class Tour(
    val id: String,
    val name: String,
    val openRegistrationDate: Timestamp,
    val closeRegistrationDate: Timestamp,
    val cancellationDeadline: Timestamp,
    val startDate: Timestamp,
    val slotQuantity: Int,
    val image: String,
    val bookingCount: Int,
    var averageRating: Double,
    val description: String,
    val destinationId: String
){
    constructor() : this("", "", Timestamp.now(), Timestamp.now(),
        Timestamp.now(), Timestamp.now(), 0, "",0, 0.0, "", "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as Tour
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

data class CategoryOfTour(
    val id:String,
    val tourId: String,
    val categoryId: String
){
    constructor():this("", "", "")
}

data class Ticket(
    val id: String,
    val ticketType: TICKET_TYPE,
    val moneyPrice: Long,
    val coinPrice: Long,
    val tourId: String
){
    constructor() : this("", TICKET_TYPE.ADULT, 0, 0, "")

}

data class CoinPackage(
    val id: String,
    val name: String,
    val coinValue: Int,
    val price: Double,
    val description: String
){
    constructor(): this("", "", 0, 0.0, "")
}

data class Bill(
    val id: String,
    val accountId: String,
    val coinPackageId: String,
    val totalAmount: Double,
    val createdDate: Timestamp = Timestamp.now(),
    val paymentStatus: PAYMENT_STATUS,
    val couponId: String = ""
){
    constructor() : this("", "", "", 0.0, Timestamp.now(), PAYMENT_STATUS.PENDING)
}

data class Account(
    val id: String,
    val userName: String = "",
    val avatar: String = "",
    val role: ACCOUNT_ROLE,
    val coin: Long = 0,
    val customerId: String = "",
    val status: ACCOUNT_STATUS
) {
    constructor() : this("", "", "", ACCOUNT_ROLE.USER, 0, "", ACCOUNT_STATUS.ACTIVE)
}

data class WishListItem(
    val id: String,
    val accountId: String,
    val tourId: String
){
    constructor():this("", "", "")
}

open class Customer (
    val id: String,
    val fullName: String,
    val gender: Boolean,
    val dateOfBirth: Timestamp,
    val address: String,
    val phoneNumber: String,
){
    constructor() : this("", "", true, Timestamp.now(), "", "")
    fun copy(
        id: String = this.id,
        fullName: String = this.fullName,
        gender: Boolean = this.gender,
        dateOfBirth: Timestamp = this.dateOfBirth,
        address: String = this.address,
        phoneNumber: String = this.phoneNumber
    ): Customer {
        return Customer(id, fullName, gender, dateOfBirth, address, phoneNumber)
    }
}

data class Staff(val position: Int): Customer("", "", true, Timestamp.now(), "", ""){
    constructor(): this (0)
}

data class StaffGuideTour(
    val id: String,
    val staffId: String,
    val tourId: String
)

data class Review(
    val id: String,
    val rating: Double,
    val comment: String,
    val createdDate: Timestamp = Timestamp.now(),
    val accountId: String,
    val tourId: String,
    val tourBookingBillId: String,
){
    constructor() : this("", 0.0, "", Timestamp.now(), "", "", "")
}

data class TourBooking(
    val id: String,
    val accountId: String,
    val ticketId: String,
    val quantity: Int,
    val total: Long,
    val valueType: VALUE_TYPE,
    val bookingDate: Timestamp,
    val cancellationDeadline: Timestamp,
    val startTourDate: Timestamp,
    val paymentStatus: PAYMENT_STATUS,
    val couponId: String = ""
) {
    constructor() : this( "", "", "", 0, 0, VALUE_TYPE.MONEY, Timestamp.now(), Timestamp.now(), Timestamp.now(), PAYMENT_STATUS.PENDING)
}

data class Coupon(
    val id: String,
    val code: String,
    val createdDate: Timestamp,
    val expiredDate: Timestamp,
    val minAmount: Long,
    val discount: Long,
    val valueType: VALUE_TYPE,
    val quantity: Int,
    val status: COUPON_STATUS
){
    constructor():this("", "", Timestamp.now(), Timestamp.now(), 0, 0, VALUE_TYPE.MONEY, 0, COUPON_STATUS.EXPIRED)
}
