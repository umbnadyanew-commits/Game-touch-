package com.gametouch

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "gametouch.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL,
                created_by TEXT,
                created_at INTEGER
            )
        """)

        insertUser(
            db,
            User(
                username = "Jerzz",
                passwordHash = hash("66"),
                role = Role.DEVELOPER,
                createdBy = "SYSTEM"
            )
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun hash(pw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(pw.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun insertUser(db: SQLiteDatabase, user: User): Long {
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.passwordHash)
            put("role", user.role.name)
            put("created_by", user.createdBy)
            put("created_at", user.createdAt)
        }
        return db.insert("users", null, cv)
    }

    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        return try {
            insertUser(db, user) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun login(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username=? AND password=?",
            arrayOf(username, hash(password))
        )
        val user = if (cursor.moveToFirst()) {
            User(
                id = cursor.getInt(0),
                username = cursor.getString(1),
                passwordHash = cursor.getString(2),
                role = Role.valueOf(cursor.getString(3)),
                createdBy = cursor.getString(4),
                createdAt = cursor.getLong(5)
            )
        } else null
        cursor.close()
        db.close()
        return user
    }

    fun getAllUsers(): List<User> {
        val list = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users ORDER BY role, username", null)
        while (cursor.moveToNext()) {
            list.add(
                User(
                    id = cursor.getInt(0),
                    username = cursor.getString(1),
                    passwordHash = cursor.getString(2),
                    role = Role.valueOf(cursor.getString(3)),
                    createdBy = cursor.getString(4),
                    createdAt = cursor.getLong(5)
                )
            )
        }
        cursor.close()
        db.close()
        return list
    }

    fun getUserByUsername(username: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=?", arrayOf(username))
        val u = if (cursor.moveToFirst()) {
            User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                Role.valueOf(cursor.getString(3)),
                cursor.getString(4),
                cursor.getLong(5)
            )
        } else null
        cursor.close()
        db.close()
        return u
    }

    fun deleteUser(id: Int): Boolean {
        val db = writableDatabase
        val rows = db.delete("users", "id=?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }

    fun updatePassword(id: Int, newPassword: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply { put("password", hash(newPassword)) }
        val rows = db.update("users", cv, "id=?", arrayOf(id.toString()))
        db.close()
        return rows > 0
    }
    }
