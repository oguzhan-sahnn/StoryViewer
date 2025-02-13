package com.example.insta

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread


class StoryViewerActivity : AppCompatActivity() {

    private lateinit var storyViewPager: ViewPager
    private lateinit var saveButton: Button
    private lateinit var storyLinks: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_viewer)

        // Gelen story linklerini al
        storyLinks = intent.getStringArrayListExtra("storyLinks") ?: arrayListOf()

        storyViewPager = findViewById(R.id.storyViewPager)
        saveButton = findViewById(R.id.saveButton)

        // ViewPager'ı adapte ediyoruz
        val adapter = StoryPagerAdapter(this, storyLinks)
        storyViewPager.adapter = adapter

        // Kaydetme butonuna tıklanıldığında hikayeyi kaydet
        saveButton.setOnClickListener {
            val currentPosition = storyViewPager.currentItem
            val storyUrl = storyLinks[currentPosition]

            // Kaydetme işlemi
            saveStory(storyUrl)
        }
    }

    // Story kaydetme işlemi
    private fun saveStory(storyUrl: String) {
        thread {
            try {
                val fileExtension = getFileExtension(storyUrl)
                val fileName = "story_${System.currentTimeMillis()}.$fileExtension"
                val file = File(filesDir, fileName)

                // Dosya türüne göre işlem yap
                if (fileExtension == "jpg" || fileExtension == "jpeg") {
                    saveImage(storyUrl, file)
                } else if (fileExtension == "mp4") {
                    saveVideo(storyUrl, file)
                } else if (fileExtension == "hoic") {
                    saveHoic(storyUrl, file)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Desteklenmeyen dosya türü", Toast.LENGTH_SHORT).show()
                    }
                    return@thread
                }

                runOnUiThread {
                    Toast.makeText(this, "Hikaye başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Hikaye kaydedilirken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Resim kaydetme
    private fun saveImage(storyUrl: String, file: File) {
        val url = URL(storyUrl)
        val inputStream = url.openStream()
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        outputStream.close()
        inputStream.close()
    }

    // Video kaydetme
    private fun saveVideo(storyUrl: String, file: File) {
        val url = URL(storyUrl)
        val inputStream = url.openStream()
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        outputStream.close()
        inputStream.close()
    }

    // HOIC dosyası kaydetme (genel bir yöntem, yine aynı şekilde indirip kaydedebiliriz)
    private fun saveHoic(storyUrl: String, file: File) {
        val url = URL(storyUrl)
        val inputStream = url.openStream()
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        outputStream.close()
        inputStream.close()
    }

    // Dosya uzantısını kontrol etme
    private fun getFileExtension(url: String): String {
        return url.substringAfterLast('.', "unknown")
    }
}
