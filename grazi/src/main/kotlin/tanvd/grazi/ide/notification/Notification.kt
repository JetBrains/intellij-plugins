package tanvd.grazi.ide.notification


import com.intellij.notification.*
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin

object Notification {
    private val NOTIFICATION_GROUP_UPDATE = NotificationGroup(GraziBundle.message("grazi.update.group"),
            NotificationDisplayType.STICKY_BALLOON, true)

    fun showUpdate(project: Project) {
        val notification = NOTIFICATION_GROUP_UPDATE.createNotification(GraziBundle.message("grazi.update.title", GraziPlugin.version),
                GraziBundle.message("grazi.update.body"), NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
        Notifications.Bus.notify(notification, project)
    }
}
