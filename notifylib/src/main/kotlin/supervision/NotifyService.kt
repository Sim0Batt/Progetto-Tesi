package supervision

interface NotifyService {
    fun notify(message: String, notifyStatus: NotifyStatus)
}