package supervision

import LoggerWriter
import org.newsclub.net.unix.AFUNIXDatagramChannel
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class SDNotifyService(logPath: String, val componentName: String): NotifyService {
    val logger = LoggerWriter(logPath, "NotifyLib::SDNotifyService")

    override fun notify(message: String, notifyStatus: NotifyStatus) {
        try{
            val socketPath = "/home/simone/workspace/etc/supervisor/sockets/$componentName.sock"
            require(File(socketPath).exists()) { "Socket file not found in $socketPath" }

            val notification = when (notifyStatus) {
                NotifyStatus.READY -> "READY=1".also { logger.info("Sent Ready notification") }
                NotifyStatus.STOPPING -> "STOPPING=1".also { logger.info("Sent Stopping notification") }
                NotifyStatus.STOPPED -> "STOPPED=1".also { logger.info("Sent Stopped notification") }
                NotifyStatus.REALOADING -> "RELOADING=1".also { logger.info("Sent Reloading notification") }
                NotifyStatus.ERRNO -> "ERRNO=1".also { logger.info("Sent Failed notification") }
                NotifyStatus.STATUS -> "STATUS=${message.replace('\n', ' ').replace('\r', ' ')}".also {
                    logger.info("Sent Status notification with message: $message")
                }
            }

            val lines = buildList {
                add(notification)
                if (message.isNotBlank() && notifyStatus != NotifyStatus.STATUS) {
                    add("STATUS=${message.replace('\n', ' ').replace('\r', ' ')}")
                }
            }
            val payload = lines.joinToString("\n").toByteArray(StandardCharsets.UTF_8)
            val address = AFUNIXSocketAddress.of(File(socketPath))

            AFUNIXDatagramChannel.open().use { channel ->
                val sentBytes = channel.send(ByteBuffer.wrap(payload), address)
                check(sentBytes == payload.size) {
                    "Sent $sentBytes of ${payload.size} notification bytes"
                }
            }
        }catch (e: Exception){
            logger.error("Failed to send notification: $e")
        }

    }
}