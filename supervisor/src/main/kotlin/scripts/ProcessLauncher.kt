package scripts

import configuration.ComponentConfiguration
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ProcessLauncher {
    fun launch(component: ComponentConfiguration, pidDir: String) {
        val command = getCommand(component)

        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val pid = reader.readLine()
        process.waitFor()

        val pidFile = File(pidDir + "${component.name}.pid")
        pidFile.createNewFile()
        pidFile.writeText(pid.toLongOrNull()?.toString() ?: "")
    }


    fun getCommand(component: ComponentConfiguration): List<String>{
        val jarCommand = "nohup java -Xmx${component.maxHeap} -DflagFile=${component.flagFile} -jar ${component.path} > ${component.logFile} 2>&1 & echo $!"
        // Separa i parametri e rimuovi gli apici singoli attorno a jarCommand
        return listOf("sh", "-c", jarCommand)
    }
}