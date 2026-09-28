import configuration.ReadXMLConfiguration
import models.Status
import scripts.LegacyPollingListener
import scripts.LoggerWriter
import scripts.ProcessLauncher
import scripts.SDNotifyListener
import java.io.File

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        val store: MutableMap<String, Status> = mutableMapOf()
        val config = ReadXMLConfiguration.getConfiguration()
        val mainLogger = LoggerWriter(config.logFile!!, "Supervisor")
        val supervisorPidFile = config.pidDir + "supervisor.pid"

        when{
            args.contains("start") -> {
                try{
                    val myPid = ProcessHandle.current().pid()
                    File(supervisorPidFile).writeText(myPid.toString())

                    mainLogger.info("Configuration Loaded, Components: ${config.components?.joinToString("\n")}")

                    config.components?.forEach {
                        store[it.name!!] = Status.STOPPED
                        ProcessLauncher.launch(it, config.pidDir)
                        mainLogger.info("Component ${it.name} launched")
                        LoggerWriter(it.logFile, "Supervisor").info("Component ${it.name} started from Supervisor")
                        val listener = SDNotifyListener(it.name!!, config.socketDir, mainLogger, config.jsonDir)
                        listener.start()
                        val legacyListener = LegacyPollingListener(1000, store, it.name!!, it.flagFile!!, mainLogger)
                        legacyListener.start()
                    }
                }catch (e: Exception){
                    mainLogger.error(e.stackTraceToString())
                }
            }

            args.contains("stop") -> {
                try{
                    val pidFile = File(supervisorPidFile)
                    if (pidFile.exists()) {
                        val pid = pidFile.readText().trim()
                        mainLogger.info("Sending kill command for Supervisor (PID: $pid)...")
                        ProcessBuilder("kill", "-15", pid).start().waitFor()
                    } else {
                        mainLogger.error("No supervisor found")
                    }
                    mainLogger.clean()
                }catch (e: Exception){
                    mainLogger.error(e.stackTraceToString())
                }
            }
        }
    }
}