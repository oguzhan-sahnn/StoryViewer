package com.example.insta

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

// Tray API yanıtı için veri sınıfı
data class TrayResponse(
    @SerializedName("tray") val tray: List<TrayItem>?
)

data class TrayItem(
    @SerializedName("id") val id: String,
    @SerializedName("user") val user: User
)

// Reels API yanıtı için veri sınıfı
data class ReelsResponse(
    @SerializedName("reels") val reels: Map<String, ReelTrayItem>?
)

data class ReelTrayItem(
    @SerializedName("id") val id: String,
    @SerializedName("user") val user: User,
    @SerializedName("items") val items: List<StoryItem>?
)

data class StoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("media_type") val mediaType: Int,
    @SerializedName("image_versions2") val imageVersions: ImageVersions?,
    @SerializedName("video_versions") val videoVersions: List<VideoVersion>?
)

data class ImageVersions(
    @SerializedName("candidates") val candidates: List<Candidate>
)

data class VideoVersion(
    @SerializedName("url") val url: String
)

data class Candidate(
    @SerializedName("url") val url: String
)

data class User(
    @SerializedName("pk") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("profile_pic_url") val profilePicUrl: String
)

object StoryFetcher {
    private val client = OkHttpClient()

    // API verilerini çekmek için gerekli sessionID ve kullanıcı PK haritalaması
    private val userPkMap = mutableMapOf<String, String>()

    // **📌 İlk API İsteği: Tray ile Kullanıcıları Getir**
    fun getActiveStoryUsers(): List<String>? {
        val sessionId = AppSession.sessionID

        if (sessionId.isNullOrEmpty()) {
            Log.e("StoryFetcher", "sessionID bulunamadı.")
            return null
        }

        val url = "https://i.instagram.com/api/v1/feed/reels_tray/"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Instagram 219.0.0.12.117 Android")
            .header("Cookie", "sessionid=$sessionId")
            .header("X-IG-App-ID", "936619743392459")
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                parseTrayResponse(jsonResponse)
            } else {
                Log.e("StoryFetcher", "API Yanıtı Başarısız -> ${response.message}")
                null
            }
        } catch (e: IOException) {
            Log.e("StoryFetcher", "API isteği sırasında hata oluştu: ${e.localizedMessage}")
            null
        }
    }

    // **📌 Tray Response Parse Et ve Kullanıcıları Çek**
    private fun parseTrayResponse(jsonResponse: String?): List<String> {
        val activeUsers = mutableListOf<String>()
        if (!jsonResponse.isNullOrEmpty()) {
            val gson = Gson()
            try {
                val trayResponse = gson.fromJson(jsonResponse, TrayResponse::class.java)
                trayResponse.tray?.forEach { trayItem ->
                    val username = trayItem.user.username
                    val userId = trayItem.user.userId

                    activeUsers.add(username)
                    userPkMap[username] = userId

                    Log.d("StoryFetcher", "Bulunan Kullanıcı: $username (ID: $userId)")
                }
            } catch (e: Exception) {
                Log.e("StoryFetcher", "JSON parse hatası: ${e.localizedMessage}")
            }
        }
        return activeUsers
    }

    // **📌 İkinci API İsteği: Reels ile Kullanıcı Hikayelerini Getir**
    fun getStoryLinksForUser(username: String): List<String>? {
        val sessionId = AppSession.sessionID
        val userId = userPkMap[username]

        if (sessionId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Log.e("StoryFetcher", "sessionID veya userID bulunamadı.")
            return null
        }

        val url = "https://i.instagram.com/api/v1/feed/reels_media/?reel_ids=$userId"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Instagram 219.0.0.12.117 Android")
            .header("Cookie", "sessionid=$sessionId")
            .header("X-IG-App-ID", "936619743392459")
            .build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                Log.d("StoryFetcher", "API Yanıtı ($username - $userId): $jsonResponse")
                parseReelsResponse(jsonResponse, userId)
            } else {
                Log.e("StoryFetcher", "API Yanıtı Başarısız -> ${response.message}")
                null
            }
        } catch (e: IOException) {
            Log.e("StoryFetcher", "API isteği sırasında hata oluştu: ${e.localizedMessage}")
            null
        }
    }

    // **📌 Reels Response Parse Et ve Hikayeleri Çek**
    private fun parseReelsResponse(jsonResponse: String?, userId: String): List<String> {
        val storyLinks = mutableListOf<String>()
        if (!jsonResponse.isNullOrEmpty()) {
            val gson = Gson()
            try {
                val reelsResponse = gson.fromJson(jsonResponse, ReelsResponse::class.java)
                reelsResponse.reels?.values?.forEach { reelTrayItem ->
                    if (reelTrayItem.user.userId == userId) {
                        reelTrayItem.items?.forEach { storyItem ->
                            if (storyItem.mediaType == 1) {
                                storyItem.imageVersions?.candidates?.firstOrNull()?.let {
                                    storyLinks.add(it.url)
                                }
                            } else if (storyItem.mediaType == 2) {
                                storyItem.videoVersions?.firstOrNull()?.let {
                                    storyLinks.add(it.url)
                                }
                            }
                        }
                    }
                }
                Log.d("StoryFetcher", "$userId kullanıcısının hikaye linkleri: $storyLinks")
            } catch (e: Exception) {
                Log.e("StoryFetcher", "JSON parse hatası: ${e.localizedMessage}")
            }
        }
        return storyLinks
    }
}
