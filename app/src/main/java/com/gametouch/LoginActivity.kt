package com.gametouch

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Session.getUsername(this) != null) {
            goToDashboard()
            return
        }

        setContentView(R.layout.activity_login)
        db = DatabaseHelper(this)

        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvInfo = findViewById<TextView>(R.id.tvInfo)

        tvInfo.text = "Login untuk masuk\nDeveloper: Jerzz / 66"

        btnLogin.setOnClickListener {
            val u = etUser.text.toString().trim()
            val p = etPass.text.toString()

            if (u.isEmpty() || p.isEmpty()) {
                toast("Isi username & password")
                return@setOnClickListener
            }

            val user = db.login(u, p)
            if (user == null) {
                toast("Username atau password salah")
            } else {
                Session.save(this, user)
                toast("Selamat datang, ${user.username} (${user.role.label})")
                goToDashboard()
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
