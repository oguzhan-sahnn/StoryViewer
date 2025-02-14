package com.example.insta

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object StoryDownloader {

    fun downloadStories(context: Context, userId: String) {
        val storyLinks = AppStories.getStoryLinks(userId)

        if (storyLinks.isEmpty()) {
            Toast.makeText(context, "İndirilecek hikaye bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        for (link in storyLinks) {
            downloadStory(context, link)
        }

        Toast.makeText(context, "Hikayeler indirildi!", Toast.LENGTH_SHORT).show()
    }

    private fun downloadStory(context: Context, urlString: String) {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            val fileName = urlString.substringAfterLast("/")
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "StoryViewer")

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.close()
            inputStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
