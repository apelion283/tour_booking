package com.project17.tourbooking.activities.user.profile.faq

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun FAQWebViewScreen(){
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            loadUrl("https://github.com/DNhat283N")
        }
    })
}
