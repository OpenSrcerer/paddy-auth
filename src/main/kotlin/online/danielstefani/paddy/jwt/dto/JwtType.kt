package online.danielstefani.paddy.jwt.dto

enum class JwtType(
    val lifetime: Long,
    val audience: String
) {
    ADMIN(31540000, "paddy~internal"),
    PAD(31540000, "paddy~pad"),
    USER(3600, "paddy~user")
}