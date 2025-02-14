package com.example.insta

object AppStories {
    private val storiesMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    // Kullanıcıya ait hikaye linklerini ekler
    fun addStoryLinks(userId: String, storyLinks: List<String>) {
        if (storiesMap.containsKey(userId)) {
            storiesMap[userId]?.addAll(storyLinks)
        } else {
            storiesMap[userId] = storyLinks.toMutableList()
        }
    }

    // Kullanıcıya ait hikaye linklerini getirir
    fun getStoryLinks(userId: String): List<String> {
        return storiesMap[userId] ?: emptyList()
    }

    // Hikaye linklerini temizler (örn: yeni veri çekildiğinde)
    fun clearStories() {
        storiesMap.clear()
    }
}
