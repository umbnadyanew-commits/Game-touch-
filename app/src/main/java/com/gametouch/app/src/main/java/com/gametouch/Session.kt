package com.gametouch

import android.content.Context

object Session {
    private const val PREF = "gametouch_session"
    private const val KEY_USER = "username"
    private const val KEY_ROLE = "role"

    fun save(context: Context, user: User) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_USER, user.username)
            .putString(KEY_ROLE, user.role.name)
            .apply()
    }

    fun getUsername(context: Context): String? =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_USER, null)

    fun getRole(context: Context): Role? {
        val r = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null) ?: return null
        return try { Role.valueOf(r) } catch (e: Exception) { null }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
