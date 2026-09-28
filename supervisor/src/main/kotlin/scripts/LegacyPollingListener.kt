package scripts

import kotlinx.serialization.json.Json
import models.Status
import models.StatusJson
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LegacyPollingListener(
    val pollingIntervalMs: Long,
    val store: MutableMap<String, Status>,
    val componentName: String,
    val flagFile: String,
    val logger: LoggerWriter
) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    fun start() {
        logger.info("Started Polling cycle for component $componentName every ${pollingIntervalMs}ms")
        if(!File(flagFile).exists()){
            File(flagFile).createNewFile()
            File(flagFile).writeText(
                StatusJson("STOPPED", "Component Flag File Created").toString()
            )
        }
        scheduler.scheduleAtFixedRate(
            ::scanFlag,
            0,
            pollingIntervalMs,
            TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        logger.info("Stopped Poll Cycle for component $componentName")
        scheduler.shutdown()
    }

    fun scanFlag(){
        try {
            val flagFile = File(flagFile)
            val serializedStatus = Json.decodeFromString<StatusJson>(flagFile.readText())
            logger.info("Setting status of $componentName from ${store[componentName]} to ${serializedStatus.status} with message ${serializedStatus.message}")
            store[componentName] = Status.valueOf(serializedStatus.status)
        }catch (e: Exception){
            logger.error("Error during flag files read for component $componentName: ${e.message}")
            throw e
        }
    }
}