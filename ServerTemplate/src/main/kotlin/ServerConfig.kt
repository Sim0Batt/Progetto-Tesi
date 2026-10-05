import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import supervision.NotifyStatus
import java.io.File


val port = System.getProperty("port")?.toInt() ?: 8080

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    //CORS Setup
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost()

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowCredentials = true

    }

    val serviceName = "test1"

    val logFile = "/home/simone/workspace/log/$serviceName/service.log"

    if(!File(logFile).parentFile.exists()) {
        File(logFile).parentFile.mkdirs()
        File(logFile).createNewFile()
    }
    val sdNotify = NotifyServiceFactory.createSDNotify(logFile, serviceName)
    val legacyNotify = NotifyServiceFactory.createLegacy(logFile)

    monitor.subscribe(ApplicationStarted) {
        sdNotify.notify("Service $serviceName started and ready", NotifyStatus.READY)
        legacyNotify.notify("Service $serviceName started and ready", NotifyStatus.READY)
    }

    monitor.subscribe(ApplicationStopping) {
        sdNotify.notify("Service $serviceName is stopping", NotifyStatus.STOPPING)
        legacyNotify.notify("Service $serviceName is stopping", NotifyStatus.STOPPING)
    }

    monitor.subscribe(ApplicationStopped) {
        sdNotify.notify("Service $serviceName is stopping", NotifyStatus.STOPPED)
        legacyNotify.notify("Service $serviceName is stopping", NotifyStatus.STOPPED)
    }

    routing {
        get("/"){
            NotifyServiceFactory.createSDNotify(logFile, serviceName)
        }

    }
}

object ServerConfig {
    fun run(args: Array<String> = emptyArray()) {
        embeddedServer(
            Netty,
            port = port,
            host = "0.0.0.0",
            module = Application::module
        ).start(wait = true)
    }
}