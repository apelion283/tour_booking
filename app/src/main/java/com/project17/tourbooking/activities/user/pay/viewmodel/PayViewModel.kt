package com.project17.tourbooking.activities.user.pay.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project17.tourbooking.api.CreateOrder
import com.project17.tourbooking.models.CoinPackage
import com.project17.tourbooking.models.Ticket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

sealed class PaymentItem{
    data class TicketItem(val ticket: Ticket): PaymentItem()
    data class CoinPackageItem(val coinPackage: CoinPackage): PaymentItem()
}

class PayViewModel: ViewModel() {
    private val _paymentItem = MutableLiveData<PaymentItem>()
    val paymentItem: LiveData<PaymentItem> = _paymentItem

    private val _quantity = MutableLiveData<Int>()
    val quantity: LiveData<Int> = _quantity

    fun setPaymentItem(item: PaymentItem){
        _paymentItem.value = item
    }

    fun setQuantity(quantity: Int){
        _quantity.value = quantity
    }

    fun isCoinPaymentAllowed(): Boolean{
        return when(_paymentItem.value){
            is PaymentItem.TicketItem -> {
                true
            }
            is PaymentItem.CoinPackageItem -> {
                _quantity.value = 1
                false
            }
            null -> false
        }
    }

    fun payWithZaloPay(
        amount: Int,
        onError: () -> Unit = {},
        onSuccess: () -> Unit = {},
        onCancel: () -> Unit = {},
        context: Context
    ){
        val scope = CoroutineScope(Dispatchers.Main)
        ZaloPaySDK.init(554, Environment.SANDBOX)
        val orderApi = CreateOrder()
        scope.launch {
            try {
                val data = orderApi.createOrder(amount.toString())
                val code = data.getString("return_code")
                if (code == "1") {
                    val token = data.getString("zp_trans_token")
                    ZaloPaySDK.getInstance().payOrder(
                        context as Activity,
                        token,
                        "demozpdk://app",
                        object : PayOrderListener {
                            override fun onPaymentCanceled(
                                zpTransToken: String?,
                                appTransID: String?
                            ) {
                                onCancel()
                            }

                            override fun onPaymentError(
                                zaloPayError: ZaloPayError?,
                                zpTransToken: String?,
                                appTransID: String?
                            ) {
                                if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                                    ZaloPaySDK.getInstance().navigateToZaloOnStore(context)
                                    ZaloPaySDK.getInstance()
                                        .navigateToZaloPayOnStore(context)
                                }
                                onError()
                            }

                            override fun onPaymentSucceeded(
                                transactionId: String,
                                transToken: String,
                                appTransID: String?
                            ) {
                                onSuccess()
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ZaloPayError", "Exception: ${e.message}")
            }
        }
    }
}