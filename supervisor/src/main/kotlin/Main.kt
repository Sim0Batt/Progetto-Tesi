import configuration.ReadXMLConfiguration
import scripts.LoggerWriter
import scripts.ProcessLauncher

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        val config = ReadXMLConfiguration.getConfiguration()
        val mainLogger = LoggerWriter(config.logFile + "/service.log", "Supervisor")

        mainLogger.info("Configuration Loaded, Components: ${config.components?.joinToString("\n")}")

        config.components?.forEach {
            ProcessLauncher.launch(it)
            mainLogger.info("Component ${it.name} launched")
            LoggerWriter(it.logFile, "Supervisor").info("Component ${it.name} started from Supervisor")
        }
    }
}