import configuration.ReadXMLConfiguration
import models.Status
import scripts.LegacyPollingListener
import scripts.LoggerWriter
import scripts.ProcessLauncher
import scripts.SDNotifyListener
import scripts.StatusManager
import java.io.File

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        val store: MutableMap<String, Status> = mutableMapOf()
        val config = ReadXMLConfiguration.getConfiguration()
        val mainLogger = LoggerWriter(config.logFile!!, "Supervisor")
        val supervisorPidFile = config.pidDir + "supervisor.pid"
        val processLauncher = ProcessLauncher(config.pidDir, mainLogger)
        val supervisioner = StatusManager(config.components!!)

        when{
            args.contains("start") -> {
                try{
                    val myPid = ProcessHandle.current().pid()
                    File(supervisorPidFile).writeText(myPid.toString())

                    mainLogger.info("Configuration Loaded, Components: ${config.components?.joinToString("\n")}")

                    val sdListeners = mutableListOf<SDNotifyListener>()
                    val legacyListeners = mutableListOf<LegacyPollingListener>()

                    Runtime.getRuntime().addShutdownHook(Thread {
                        mainLogger.info("Spegnimento in corso, fermo i componenti...")

                        config.components!!.forEach { component ->
                            processLauncher.stopComponent(component.name!!)
                        }

                        sdListeners.forEach { it.stop() }
                        legacyListeners.forEach { it.stop() }
                        File(supervisorPidFile).delete()
                    })

                    config.components?.forEach {
                        File(it.logFile).parentFile.mkdirs()
                        store[it.name!!] = Status.STOPPED

                        val listener = SDNotifyListener(it.name!!, config.socketDir, mainLogger, config.jsonDir)
                        listener.start()
                        sdListeners.add(listener)

                        processLauncher.launch(it)
                        mainLogger.info("Component ${it.name} launched")

                        val legacyListener = LegacyPollingListener(1000, store, it.name!!, it.flagFile!!, mainLogger)
                        legacyListener.start()
                        legacyListeners.add(legacyListener)
                    }

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

            args.contains("stopcomponent") -> {
                processLauncher.stopComponent(args[1])
                mainLogger.info("Component ${args[1]} stopped")
            }

            args.contains("startcomponent") -> {
                processLauncher.startComponent(args[1], config.components!!)
                mainLogger.info("Component ${args[1]} started")
            }

            args.contains("status") -> {
                while(true){
                    println("\u001B[2J\u001B[H")
                    println(supervisioner.getString())
                    Thread.sleep(1000)
                }
            }
        }
    }
}

/* COMANDO DI LANCIO
nohup /home/simone/.sdkman/candidates/java/25.0.3-tem/bin/java \
  -jar /home/simone/workspace/Progetto-Tesi/supervisor/build/libs/supervisor.jar start \
  </dev/null >/tmp/supervisor-startup.log 2>&1 &
*/