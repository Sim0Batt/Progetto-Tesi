import supervision.LegacyService
import supervision.NotifyService
import supervision.SDNotifyService

object NotifyServiceFactory {
    fun createSDNotify(logPath: String): NotifyService {
        return SDNotifyService(logPath)
    }

    fun createLegacy(logPath: String): NotifyService {
        return LegacyService(logPath)
    }
}