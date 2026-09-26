package configuration

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element

class ComponentConfiguration {
    @field:Attribute(name = "name", required = true)
    var name: String? = null
    @field:Attribute(name = "path", required = true)
    var path: String? = null
    @field:Attribute(name = "notify", required = false)
    var notify: String? = "LEGACY"
    @field:Attribute(name = "logFile", required = true)
    var logFile: String = ""
    @field:Attribute(name = "maxHeap", required = false)
    var maxHeap: String? = "2G"

    override fun toString(): String {
        return "ComponentConfiguration(name=$name, path=$path, notify=$notify, logFile=$logFile, maxHeap=$maxHeap)"
    }
}