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

                    val sdListeners = mutableListOf<SDNotifyListener>()
                    val legacyListeners = mutableListOf<LegacyPollingListener>()


                    config.components?.forEach {
                        File(it.logFile).mkdirs()
                        File(it.logFile).createNewFile()
                        store[it.name!!] = Status.STOPPED
                        ProcessLauncher.launch(it, config.pidDir)
                        mainLogger.info("Component ${it.name} launched")
                        LoggerWriter(it.logFile, "Supervisor").info("Component ${it.name} started from Supervisor")
                        val listener = SDNotifyListener(it.name!!, config.socketDir, mainLogger, config.jsonDir)
                        listener.start()
                        val legacyListener = LegacyPollingListener(1000, store, it.name!!, it.flagFile!!, mainLogger)
                        legacyListener.start()

                        sdListeners.add(listener)
                        legacyListeners.add(legacyListener)
                    }

                    Runtime.getRuntime().addShutdownHook(Thread {
                        mainLogger.info("Spegnimento in corso, fermo i listener...")
                        sdListeners.forEach { it.stop() }
                        legacyListeners.forEach { it.stop() }
                    })

                    // Tieni in vita il main thread
                    Thread.currentThread().join()

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