import configuration.ReadXMLConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object Main{
    @JvmStatic
    fun main(args: Array<String>) {
        val logger: Logger = LoggerFactory.getLogger(Main::class.java)

        val config = ReadXMLConfiguration.getConfiguration()

        logger.info(config.components?.joinToString())

        println("Hello World!")
    }
}