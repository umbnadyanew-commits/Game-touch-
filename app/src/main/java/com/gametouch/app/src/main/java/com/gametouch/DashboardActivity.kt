package com.gametouch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val username = Session.getUsername(this) ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val role = Session.getRole(this) ?: Role.MEMBER

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvRole = findViewById<TextView>(R.id.tvRole)
        val container = findViewById<LinearLayout>(R.id.containerButtons)

        tvWelcome.text = "Halo, $username"
        tvRole.text = "Role: ${role.label}"

        when (role) {
            Role.DEVELOPER -> {
                addButton(container, "Kelola User") {
                    startActivity(Intent(this, ManageUserActivity::class.java))
                }
                addButton(container, "Fitur Developer") { toast("Fitur Developer") }
                addButton(container, "Log Aktivitas") { toast("Log Aktivitas") }
            }
            Role.OWNER -> {
                addButton(container, "Kelola Reseller") {
                    startActivity(Intent(this, ManageUserActivity::class.java))
                }
                addButton(container, "Lihat Member") { toast("Lihat Member") }
                addButton(container, "Statistik") { toast("Statistik") }
            }
            Role.RESELLER -> {
                addButton(container, "Kelola Member") {
                    startActivity(Intent(this, ManageUserActivity::class.java))
                }
                addButton(container, "Buat Akun Member") { toast("Buat Member") }
            }
            Role.MEMBER -> {
                addButton(container, "Mulai Game") {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                addButton(container, "Profil Saya") { toast("Profil") }
            }
        }

        addButton(container, "Logout") {
            Session.clear(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun addButton(parent: LinearLayout, text: String, action: () -> Unit) {
        val btn = Button(this).apply {
            this.text = text
            setOnClickListener { action() }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 16, 0, 0)
            layoutParams = lp
        }
        parent.addView(btn)
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
