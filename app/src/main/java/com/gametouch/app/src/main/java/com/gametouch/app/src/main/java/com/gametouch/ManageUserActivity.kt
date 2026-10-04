package com.gametouch

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ManageUserActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: UserAdapter
    private lateinit var currentRole: Role
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)

        db = DatabaseHelper(this)
        currentRole = Session.getRole(this) ?: Role.MEMBER
        currentUser = Session.getUsername(this) ?: ""

        val rv = findViewById<RecyclerView>(R.id.rvUsers)
        val btnAdd = findViewById<Button>(R.id.btnAddUser)

        adapter = UserAdapter(
            onDelete = { user -> confirmDelete(user) },
            onReset = { user -> resetPassword(user) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnAdd.setOnClickListener { showAddDialog() }

        loadUsers()
    }

    private fun loadUsers() {
        val all = db.getAllUsers()
        val filtered = when (currentRole) {
            Role.DEVELOPER, Role.OWNER -> all
            Role.RESELLER -> all.filter {
                it.role == Role.MEMBER && it.createdBy == currentUser
            }
            else -> emptyList()
        }
        adapter.submit(filtered)
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val etUser = view.findViewById<EditText>(R.id.etNewUsername)
        val etPass = view.findViewById<EditText>(R.id.etNewPassword)
        val spRole = view.findViewById<Spinner>(R.id.spRole)

        val allowedRoles = when (currentRole) {
            Role.DEVELOPER -> listOf(Role.OWNER, Role.RESELLER, Role.MEMBER)
            Role.OWNER -> listOf(Role.RESELLER, Role.MEMBER)
            Role.RESELLER -> listOf(Role.MEMBER)
            else -> emptyList()
        }

        spRole.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            allowedRoles.map { it.label }
        )

        AlertDialog.Builder(this)
            .setTitle("Buat Akun Baru")
            .setView(view)
            .setPositiveButton("Buat") { _, _ ->
                val u = etUser.text.toString().trim()
                val p = etPass.text.toString()
                val role = allowedRoles[spRole.selectedItemPosition]

                if (u.isEmpty() || p.isEmpty()) {
                    Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (db.getUserByUsername(u) != null) {
                    Toast.makeText(this, "Username sudah dipakai", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newUser = User(
                    username = u,
                    passwordHash = db.hash(p),
                    role = role,
                    createdBy = currentUser
                )
                val ok = db.insertUser(newUser)
                Toast.makeText(
                    this,
                    if (ok) "Akun $u (${role.label}) dibuat" else "Gagal buat akun",
                    Toast.LENGTH_SHORT
                ).show()
                loadUsers()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete(user: User) {
        if (user.role == Role.DEVELOPER) {
            Toast.makeText(this, "Developer tidak bisa dihapus", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun?")
            .setMessage("Yakin hapus ${user.username} (${user.role.label})?")
            .setPositiveButton("Hapus") { _, _ ->
                db.deleteUser(user.id)
                loadUsers()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun resetPassword(user: User) {
        val input = EditText(this).apply { hint = "Password baru" }
        AlertDialog.Builder(this)
            .setTitle("Reset Password ${user.username}")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val np = input.text.toString()
                if (np.isNotEmpty()) {
                    db.updatePassword(user.id, np)
                    Toast.makeText(this, "Password direset", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
