package com.example.insta

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

// Data class to parse Instagram story response
data class StoryResponse(
    @SerializedName("tray") val tray: List<ReelTrayItem>?
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

    fun getActiveStoryUsers(): List<String>? {
        val sessionId = AppSession.sessionID
        val userId = AppSession.userID

        if (sessionId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Log.e("StoryFetcher", "sessionID veya userID bulunamadı.")
            return null
        }

        val url = "https://i.instagram.com/api/v1/feed/reels_tray/"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Instagram 219.0.0.12.117 Android")
            .header("Cookie", "sessionid=$sessionId")
            .header("X-IG-App-ID", "936619743392459")
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
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
                storyResponse.tray?.forEach { reelTrayItem ->
                    activeUsers.add(reelTrayItem.user.username)
                }
            } catch (e: Exception) {
                Log.e("StoryFetcher", "JSON parse hatası: ${e.localizedMessage}")
            }
        }
        return activeUsers
    }

    // 🔥 Burası yeni eklenen fonksiyon! Bir kullanıcının tüm hikaye linklerini alır. 🔥
    fun getStoryLinksForUser(userId: String): List<String>? {
        val sessionId = AppSession.sessionID


        if (sessionId.isNullOrEmpty()) {
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

        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonResponse = response.body?.string()
                return extractStoryLinks(jsonResponse, userId)
            } else {
                Log.e("StoryFetcher", "API Yanıtı Başarısız -> ${response.message}")
            }
        } catch (e: IOException) {
            Log.e("StoryFetcher", "API isteği sırasında hata oluştu: ${e.localizedMessage}")
        }
        return null
    }

    private fun extractStoryLinks(jsonResponse: String?, id: String): List<String> {
        val storyLinks = mutableListOf<String>()
        if (!jsonResponse.isNullOrEmpty()) {
            val gson = Gson()
            try {
                val storyResponse = gson.fromJson(jsonResponse, StoryResponse::class.java)
                storyResponse.tray?.forEach { reelTrayItem ->
                    if (reelTrayItem.user.userId == id) {
                        reelTrayItem.items?.forEach { storyItem ->
                            if (storyItem.mediaType == 1) {
                                // Fotoğraf hikaye
                                storyItem.imageVersions?.candidates?.firstOrNull()?.let {
                                    storyLinks.add(it.url)
                                }
                            } else if (storyItem.mediaType == 2) {
                                // Video hikaye
                                storyItem.videoVersions?.firstOrNull()?.let {
                                    storyLinks.add(it.url)
                                }
                            }
                        }
                    }
                }
                Log.d("StoryFetcher", "$id kullanıcısının hikaye linkleri: $storyLinks")
            } catch (e: Exception) {
                Log.e("StoryFetcher", "JSON parse hatası: ${e.localizedMessage}")
            }
        }
        return storyLinks
    }
}
