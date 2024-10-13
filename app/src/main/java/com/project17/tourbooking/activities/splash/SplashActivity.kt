package com.project17.tourbooking.activities.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.project17.tourbooking.activities.MainActivity
import com.project17.tourbooking.ui.theme.TourBookingTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen(){
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(2000L)
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }
}