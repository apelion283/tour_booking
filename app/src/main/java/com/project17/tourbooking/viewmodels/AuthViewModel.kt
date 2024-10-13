package com.project17.tourbooking.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project17.tourbooking.constant.ACCOUNT_ROLE
import com.project17.tourbooking.constant.ACCOUNT_STATUS
import com.project17.tourbooking.constant.DEFAULT_AVATAR
import com.project17.tourbooking.constant.GENDER
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Account
import com.project17.tourbooking.models.Customer

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState


    init {
        checkAuthStatus()
    }

    val currentUserEmail: String? get() = auth.currentUser?.email

    private fun checkAuthStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    fun login(usernameOrEmail: String, password: String, onResult: (String?) -> Unit) {
        if (usernameOrEmail.contains("@")) {
            auth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                        onResult(null)
                    } else {
                        onResult(task.exception?.message)
                    }
                }
        } else {
//            login with username
        }
    }

    fun signUp(email: String, password: String, fullName: String, userName: String) {
        try {
            var accountToAdd = Account(
                "",
                userName,
                DEFAULT_AVATAR,
                ACCOUNT_ROLE.USER,
                0,
                "",
                ACCOUNT_STATUS.ACTIVE
            )

            _authState.value = AuthState.Loading
            Log.d("SignUp", "Attempting to create user with email: $email")

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("SignUp", "User created successfully")

                        // Cập nhật accountToAdd với uid của người dùng mới
                        auth.currentUser?.let {
                            accountToAdd = accountToAdd.copy(id = it.uid)
                            Log.d("SignUp", "User ID: ${it.uid}")
                        }

                        _authState.value = AuthState.SignUpSuccess

                        val customerId = FirestoreHelper.createCustomer(
                            Customer(
                                "",
                                fullName,
                                GENDER.MALE.toBoolean(),
                                Timestamp.now(),
                                "",
                                ""
                            )
                        )
                        accountToAdd = accountToAdd.copy(customerId = customerId)
                        Log.d("SignUp", "Customer ID: $customerId")

                        // Thêm tài khoản vào Firestore
                        FirestoreHelper.createAccount(accountToAdd)
                        Log.d("SignUp", "Account added to Firestore: $accountToAdd")
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.message ?: "Something went wrong")
                        Log.e("SignUp", "SignUp failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            _authState.value =
                AuthState.Error(e.message ?: "Something went wrong")
            Log.e("SignUp", "Exception occurred: ${e.message}")
        }
    }



    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun changePassword(newPassword: String){
        auth.currentUser?.updatePassword(newPassword)
    }

}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    object SignUpSuccess : AuthState()
}
