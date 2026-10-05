package scripts

import models.Status
import models.StatusJson
import org.newsclub.net.unix.AFUNIXDatagramChannel
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.concurrent.thread

class SDNotifyListener(val componentName: String, val socketPath: String, val logger: LoggerWriter, val jsonDir: String){
    private var channel: AFUNIXDatagramChannel? = null
    fun start(){
        val socketFile = File("$socketPath/$componentName.sock")
        if (socketFile.exists()) {
            socketFile.delete()
        }
        val address = AFUNIXSocketAddress.of(socketFile)
        channel = AFUNIXDatagramChannel.open()
        channel!!.bind(address)
        logger.info("Started Listener $componentName su $socketPath$componentName.sock")
        thread (isDaemon = true, name = "Listener-$componentName") {
            val buffer = ByteBuffer.allocate(1024)
            while (true) {
                buffer.clear()
                channel!!.receive(buffer)
                buffer.flip()
                val message = String(buffer.array(), 0, buffer.limit())
                var state: Status? = null
                message.lines().forEach { line ->
                    when {
                        line == "READY=1" -> {
                            logger.info("Component $componentName is ready")
                            state = Status.READY
                        }
                        line == "STOPPING=1" ->  {
                            logger.info("Component $componentName is stopping")
                            state = Status.STOPPING
                        }
                        line == "STOPPED=1" -> {
                            logger.info("Component $componentName stopped")
                            state = Status.STOPPED
                        }
                        line == "RELOADING=1" -> {
                            logger.info("Component $componentName is reloading")
                            state = Status.REALOADING
                        }
                        line.startsWith("ERRNO=") -> {
                            logger.info("Component $componentName exited with error: ${line.substring(6)}")
                            state = Status.ERRNO
                        }
                    }
                }
                if (state != null) {
                    if(File("$jsonDir$componentName.json").exists()){
                        File("$jsonDir$componentName.json").writeText(StatusJson(state!!.name, "").toString())
                    }else{
                        File("$jsonDir$componentName.json").createNewFile()
                        File("$jsonDir$componentName.json").writeText(StatusJson(state!!.name, "").toString())
                    }
                }
            }
        }
    }


    fun stop() {
        logger.info("Stopped legacy checker for $componentName")
        channel?.close()
    }
}