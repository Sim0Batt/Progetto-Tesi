package scripts

import configuration.ComponentConfiguration
import kotlinx.serialization.json.Json
import models.StatusJson
import java.io.File
import kotlin.collections.map

class StatusManager(val components: List<ComponentConfiguration>) {
    val BOLD = "\u001B[1m"
    val RED = "\u001B[31m"
    val GREEN = "\u001B[32m"
    val RESET = "\u001B[0m"

    fun getString(): String{
        val tmp = mutableMapOf<String, StatusJson>()
        components.map {
            tmp[it.name!!] = Json.decodeFromString<StatusJson>(File(it.flagFile!!).readText())
        }

        var statusString =  """
-----------------------------------------------------
                   Supervisioner
-----------------------------------------------------
""".trimIndent()
        statusString += "\n"
        tmp.forEach { (name, json) ->
            statusString += "$BOLD$name$RESET: ${if (json.status == "READY") GREEN else RED}${json.status}${RESET}\n"
        }

        return statusString
    }
}