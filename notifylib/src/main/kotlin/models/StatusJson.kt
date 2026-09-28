package models
import kotlinx.serialization.Serializable


@Serializable
class StatusJson(val status: String, val message: String) {
    override fun toString(): String {
        return """{"status":"$status", "message": "$message"}"""
    }
}