package com.example.insta

object AppSession {
    var sessionID: String? = null
    var userID: String? = null

    fun saveSession(session: String, user: String) {
        sessionID = session
        userID = user

    }
}
