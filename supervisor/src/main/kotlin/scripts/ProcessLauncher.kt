package scripts

import configuration.ComponentConfiguration
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ProcessLauncher(val pidDir: String, val logger: LoggerWriter) {
    fun launch(component: ComponentConfiguration) {
        val javaBin = ProcessHandle.current().info().command()
            .orElse(System.getProperty("java.home") + "/bin/java")

        val logFile = File(component.logFile)
        logFile.parentFile?.mkdirs()

        val process = ProcessBuilder(
            javaBin,
            "-Xmx${component.maxHeap}",
            "-DflagFile=${component.flagFile}",
            "-Dport=${component.port}",
            "--enable-native-access=ALL-UNNAMED",
            "-jar", component.path!!
        )
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
            .start()

        File(pidDir + "${component.name}.pid").writeText(process.pid().toString())
    }

    fun stopComponent(componentName: String): Boolean {
        return try {
            val pidFile = File(pidDir, "$componentName.pid")
            val pid = pidFile.readText().trim().toLong()
            val process = ProcessHandle.of(pid).orElse(null)

            if (process != null && process.isAlive) {
                process.destroy()

                try {
                    process.onExit().get(3, TimeUnit.SECONDS)
                } catch (exception: TimeoutException) {
                    logger.info("Forcing termination of $componentName")
                    process.destroyForcibly()
                    process.onExit().get(5, TimeUnit.SECONDS)
                }
            }

            pidFile.delete()
            logger.info("Component $componentName stopped")
            true
        } catch (exception: Exception) {
            logger.error(
                "Failed to stop component $componentName: " +
                        exception.stackTraceToString()
            )
            false
        }
    }

    fun startComponent(componentName: String, components: List<ComponentConfiguration>){
        val component = components.find { it.name == componentName }
        if (component != null){
            launch(component)
        }else{
            logger.info("Component $componentName not found")
        }
    }
}