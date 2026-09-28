package models

import kotlinx.serialization.Serializable

@Serializable
class StatusJson (
    val status: String,
    val message: String? = null
){
    override fun toString(): String {
        return """{"status": "$status", "message": "$message"}"""
    }
}