package scripts

import configuration.ComponentConfiguration
import java.io.BufferedReader
import java.io.InputStreamReader

object ProcessLauncher {
    fun startProcess(component: ComponentConfiguration): Long {
        val command = getCommand(component)

        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val pid = reader.readLine()
        process.waitFor()

        return pid.toLongOrNull() ?: throw Exception("Impossible to retrieve the PID")
    }


    fun getCommand(component: ComponentConfiguration): String{
        val javaCommand = "nohup java -Xmx${component.maxHeap} -jar ${component.path} > ${component.logFile} 2>&1 & echo $!"
        return "sh -c '$javaCommand'"
    }
}