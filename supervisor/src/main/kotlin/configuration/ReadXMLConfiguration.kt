package configuration

import org.simpleframework.xml.core.Persister
import java.io.File

var mainPath : String = "/home/gaia/etc/supervisor/settings.xml"
val windowsPath: String = "C:\\Users\\sbattisti\\progetti\\tmp\\supervisor\\settings.xml"
var macPath : String = "/Users/Simone/workspace/etc/supervisor/settings.xml"

var linuxPath : String = "/home/simone/workspace/etc/supervisor/settings.xml"


object ReadXMLConfiguration {
    fun getConfiguration() : SupervisorConfiguration {
        val xmlFile = if(File(mainPath).exists()) {
            File(mainPath)
        }else if(File(windowsPath).exists()) {
            File(windowsPath)
        }else if ( File(macPath).exists()){
            File(macPath)
        }else if ( File(linuxPath).exists()){
            File(linuxPath)
        }else{
            throw Exception("Configuration Read: File Not Found")
        }
        val serializer = Persister()
        val configuration = serializer.read(SupervisorConfiguration::class.java, xmlFile)

        return configuration
    }
}

