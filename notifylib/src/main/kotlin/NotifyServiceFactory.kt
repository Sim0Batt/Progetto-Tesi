import supervision.LegacyService
import supervision.NotifyService
import supervision.SDNotifyService

object NotifyServiceFactory {
    fun createSDNotify(logPath: String, componentName: String): NotifyService {
        return SDNotifyService(logPath, componentName)
    }

    fun createLegacy(logPath: String): NotifyService {
        return LegacyService(logPath)
    }
}