package com.example.insta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

class StoryViewerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var storyLinks: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_viewer) // XML dosyan olacak!

        // Intent ile gelen hikaye linklerini al
        storyLinks = intent.getStringArrayListExtra("storyLinks") ?: ArrayList()

        // ViewPager'ı başlat
        viewPager = findViewById(R.id.viewPager)
        val adapter = StoryPagerAdapter(this, storyLinks)
        viewPager.adapter = adapter
    }
}
