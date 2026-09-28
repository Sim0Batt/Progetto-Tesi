package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Configuration")
class SupervisorConfiguration {
    @field:Element(name = "Log", required = true)
    var log: String? = null

    @field:Element(name = "LogFile", required = true)
    var logFile: String? = null

    @field:Element(name = "PidDir", required = true)
    var pidDir: String = ""

    @field:Element(name = "SocketDir", required = true)
    var socketDir: String = ""

    @field:Element(name = "JsonDir", required = true)
    var jsonDir: String = ""

    @field:ElementList(name = "Components", required = true)
    var components: MutableList<ComponentConfiguration>? = mutableListOf()
}