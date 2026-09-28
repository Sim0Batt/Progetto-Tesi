package supervision

import LoggerWriter
import info.faljse.SDNotify.SDNotify

class SDNotifyService(logPath: String): NotifyService {
    val logger = LoggerWriter(logPath, "SDNotifyService")

    override fun notify(message: String, notifyStatus: NotifyStatus) {
        try{
            val socketPath = System.getenv("NOTIFY_SOCKET")
            if (socketPath.isNullOrBlank()) throw Exception("NOTIFY_SOCKET environment variable is not set")

            val command: MutableList<String> = mutableListOf("systemd-notify ")
            when (notifyStatus) {
                NotifyStatus.READY -> {
                    command.add("--pid=${ProcessHandle.current().pid()} ")
                    command.add("--READY=1")
                    logger.info("Sent Ready notification")
                }

                NotifyStatus.STOPPING -> {
                    command.add("--STOPPING=1")
                    logger.info("Sent Stopping notification")
                }

                NotifyStatus.STOPPED -> {
                    command.add("--STOPPED=1")
                    logger.info("Sent Stopped notification")
                }

                NotifyStatus.REALOADING -> {
                    command.add("--RELOADING=1")
                    logger.info("Sent Reloading notification")
                }

                NotifyStatus.ERRNO -> {
                    command.add("--ERRNO=1")
                    logger.info("Sent Failed notification")
                }

                NotifyStatus.STATUS -> {
                    command.add("--STATUS=$message")
                    logger.info("Sent Status notification with message: $message")
                }

            }

            if (message.isNotBlank() && notifyStatus != NotifyStatus.STATUS) {
                command.add("--STATUS=$message")
            }

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                logger.error("Failed to send notification, exit code $exitCode: $output")
                throw Exception("Failed to send notification, exit code $exitCode: $output")
            }
        }catch (e: Exception){
            logger.error("Failed to send notification: $e")
        }

    }
}