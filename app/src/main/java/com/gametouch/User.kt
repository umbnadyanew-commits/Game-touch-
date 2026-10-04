package com.gametouch

enum class Role(val label: String) {
    DEVELOPER("Developer"),
    OWNER("Owner"),
    RESELLER("Reseller"),
    MEMBER("Member")
}

data class User(
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val role: Role,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
