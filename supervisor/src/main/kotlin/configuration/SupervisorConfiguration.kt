package configuration

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Configuration")
class SupervisorConfiguration {
    @field:Element(name = "Log", required = true)
    var log: String? = null

    @field:ElementList(name = "Components", required = true)
    var components: MutableList<ComponentConfiguration>? = mutableListOf()
}