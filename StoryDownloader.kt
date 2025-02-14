package com.example.insta

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL

object StoryDownloader {

    private val cachedStories = mutableMapOf<String, List<Bitmap>>() // Kullanıcıya göre bellekte tutulan hikayeler.

    fun downloadStories(context: Context, userId: String) {
        val storyLinks = AppStories.getStoryLinks(userId) // Sadece linkleri çekiyoruz.

        if (storyLinks.isEmpty()) {
            Toast.makeText(context, "İndirilecek hikaye bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val storyBitmaps = mutableListOf<Bitmap>()

        for (link in storyLinks) {
            val bitmap = downloadStory(link)
            if (bitmap != null) {
                storyBitmaps.add(bitmap) // Bellekte saklıyoruz.
            }
        }

        if (storyBitmaps.isNotEmpty()) {
            cachedStories[userId] = storyBitmaps // Kullanıcıya özel bellekte tutuyoruz.
        }

        Toast.makeText(context, "Hikayeler belleğe indirildi!", Toast.LENGTH_SHORT).show()
    }

    private fun downloadStory(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)

            inputStream.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCachedStories(userId: String): List<Bitmap>? {
        return cachedStories[userId] // Bellekteki hikayeleri döndürüyoruz.
    }
}
