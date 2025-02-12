package com.example.insta

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var isLoggedIn = false // Giriş yapıldı mı kontrol etmek için bir flag ekliyoruz

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        webView = findViewById(R.id.webView)

        // WebView ayarları
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true // JavaScript'i etkinleştir
        webSettings.domStorageEnabled = true // Yerel depolamayı etkinleştir

        webSettings.setSupportMultipleWindows(true)
        webSettings.loadsImagesAutomatically = true
        webSettings.allowContentAccess = true
        webSettings.allowFileAccess = true

        // WebViewClient ayarlıyoruz
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Instagram login sayfası yüklendiğinde çerezleri al
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookie(url)

                // Çerezleri işleyelim
                if (!cookies.isNullOrEmpty()) {
                    // sessionid çerezini alalım
                    val sessionID = cookies.split(";")
                        .find { it.contains("sessionid") }
                        ?.split("=")
                        ?.getOrNull(1)
                    val userID = cookies.split(";")
                        .find { it.contains("ds_user_id") }
                        ?.split("=")
                        ?.getOrNull(1)

                    // sessionID varsa, bunu kaydedelim
                    if (!sessionID.isNullOrEmpty() && !userID.isNullOrEmpty()) {
                        saveSessionData(sessionID, userID)
                        Toast.makeText(applicationContext, "Giriş Başarılı!", Toast.LENGTH_SHORT)
                            .show()

                        // Giriş başarılı olduktan sonra 20 saniye bekleyelim
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Yönlendirmeyi hemen yapma, sadece 20 saniye bekle
                        }, 20000) // 20 saniye bekle
                    }
                }
            }

            // Sayfa yüklenemediğinde error handling
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(
                    applicationContext,
                    "Sayfa Yüklenemedi: ${error?.description}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Instagram login sayfasına git
        webView.loadUrl("https://www.instagram.com/accounts/login/")
    }

    // Çerezleri kaydetmek için bir yöntem
    private fun saveSessionData(sessionID: String, userID: String) {
        // AppSession içine kaydet
        AppSession.saveSession(sessionID, userID)

        // SharedPreferences'a da kaydedelim ki uygulama kapanınca da saklansın
        val sharedPreferences = getSharedPreferences("InstagramSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("session_id", sessionID)
        editor.putString("user_id", userID)
        editor.apply()
    }

    // Ses kısma tuşuna basıldığında yönlendirmeyi başlat
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && !isLoggedIn) {
            isLoggedIn = true // Yalnızca bir kere işlemi yapalım
            startActivity(Intent(applicationContext, StoryListActivity::class.java)) // StoryListActivity'ye yönlendir
            finish() // Login ekranını kapat
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
