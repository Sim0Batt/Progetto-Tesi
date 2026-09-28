package supervision

import LoggerWriter
import models.StatusJson
import java.io.File
import kotlin.concurrent.thread

class LegacyService(logPath: String): NotifyService {
    val logger = LoggerWriter(logPath, "LegacyService")
    override fun notify(message: String, notifyStatus: NotifyStatus) {
        System.getProperty("flagFile")?.let { flagPath ->
            updateFlag(flagPath, notifyStatus)

            logger.info("Registering shutdown hook for updating the flag file")
            val thread = thread(start = false, name = "GridServletFlagUpdater") {
                updateFlag(flagPath, NotifyStatus.ERRNO, "Process shut down")
            }

            try {
                Runtime.getRuntime().addShutdownHook(thread)
            }
            catch (e: Exception) {
                logger.warning("Failed registering shutdown hook for updating grid service flag: $e")
            }
        }
    }

    fun updateFlag(path: String, state: NotifyStatus?, message: String? = null) {
        try {
            val flagFile = File(path)

            /* FAILED status must be written only after an INITIALIZING status has been written, so the flag file must
             * exist: if it doesn't exist, an external program (ex. Supervisor) has deleted it using its own logic */
            if (state == NotifyStatus.ERRNO && !flagFile.exists()) {
                logger.info("Skipping write of FAILED status on non existing flag file ${flagFile.absolutePath}")
                return
            }
            /* Avoid overwriting the status (due to the shutdown hook) if a FAILED status has already been written */
            else if (state == NotifyStatus.ERRNO && "\"status\":\"FAILED\"" in flagFile.readText()) {
                logger.info("Skipping write of FAILED status on already FAILED flag file ${flagFile.absolutePath}")
                return
            }

            val status = StatusJson(state.toString(), message ?: "")
            logger.info("Writing status $status on Grid flag file ${flagFile.absolutePath}")
            flagFile.writeText(status.toString())

        } catch (e: Throwable) {
            logger.warning("Failed writing status $state on Grid flag file $path: $e")
        }
    }
}