package com.project17.tourbooking.constant

enum class ACCOUNT_STATUS{
    ACTIVE,
    INACTIVE,
}

enum class ACCOUNT_ROLE(){
    ADMIN,
    USER,
    STAFF;
}

enum class PAYMENT_STATUS{
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}

enum class TICKET_TYPE(){
    CHILDREN,
    ADULT;

}

enum class GENDER(val genderValue: Int){
    MALE(0),
    FEMALE(1);

    fun toBoolean(): Boolean {
        return this == MALE
    }
}

enum class VALUE_TYPE{
    COIN,
    MONEY
}

enum class COUPON_STATUS{
    ACTIVE,
    EXPIRED,
    USED
}