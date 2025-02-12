package com.example.insta

import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import com.google.gson.annotations.SerializedName

// Data class to represent the response structure (based on Instagram's API response)
data class StoryResponse(
    @SerializedName("reels_Tray") val reels_Tray: List<ReelTrayItem>? // Değişken adını API'nin orijinal haliyle uyumlu hale getirdim
)

data class ReelTrayItem(
    @SerializedName("user") val user: User
)

data class User(
    @SerializedName("pk") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("is_private") val isPrivate: Boolean
)

object StoryFetcher {

    private val client = OkHttpClient()

    fun getActiveStoryUsers(): List<String>? {
        val sessionId = AppSession.sessionID
        val userId = AppSession.userID

        // Eğer sessionID veya userID boşsa API isteğini yapma
        if (sessionId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Log.e("StoryFetcher", "sessionID veya userID bulunamadı.")
            return null
        }

        val url = "https://i.instagram.com/api/v1/feed/reels_tray/"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Instagram 219.0.0.12.117 Android (30/11; 320dpi; 720x1280; Xiaomi; Redmi Note 8; willow; qcom; en_US; 325400613)") // Instagram'ın mobil uygulaması gibi göster
            .header("Cookie", "sessionid=$sessionId; ds_user_id=$userId")
            .header("X-IG-App-ID", "936619743392459")
            .build()

        Log.d("StoryFetcher", "API isteği gönderiliyor: $url")
        Log.d("StoryFetcher", "Session ID: $sessionId")
        Log.d("StoryFetcher", "User ID: $userId")

        try {
            val response: Response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                Log.d("StoryFetcher", "API Yanıtı Başarılı")
                Log.d("StoryFetcher", "Yanıt: $jsonResponse")  // API'den gelen yanıtı logla
                return parseActiveStoryUsers(jsonResponse)
            } else {
                Log.e("StoryFetcher", "API Yanıtı Başarısız -> ${response.message}")
            }
        } catch (e: IOException) {
            Log.e("StoryFetcher", "API isteği sırasında hata oluştu: ${e.localizedMessage}")
        }

        return null
    }

    private fun parseActiveStoryUsers(jsonResponse: String?): List<String> {
        val activeUsers = mutableListOf<String>()
        if (!jsonResponse.isNullOrEmpty()) {
            val gson = Gson()
            try {
                val storyResponse = gson.fromJson(jsonResponse, StoryResponse::class.java)

                // Eğer reels_tray null değilse ve boş değilse kullanıcıları listeye ekliyoruz
                storyResponse.reels_Tray?.forEach { reelTrayItem ->
                    activeUsers.add(reelTrayItem.user.username)
                }

                Log.d("StoryFetcher", "Aktif hikaye kullanıcıları: $activeUsers")
            } catch (e: Exception) {
                Log.e("StoryFetcher", "JSON parse hatası: ${e.localizedMessage}")
            }
        } else {
            Log.e("StoryFetcher", "Boş veya geçersiz API yanıtı.")
        }
        return activeUsers
    }
}
