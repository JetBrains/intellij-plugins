package tanvd.grazi.ide.notification


import com.intellij.notification.*
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.LangDownloader

object Notification {
    private val NOTIFICATION_GROUP_INSTALL = NotificationGroup(msg("grazi.install.group"),
            NotificationDisplayType.STICKY_BALLOON, true)
    private val NOTIFICATION_GROUP_UPDATE = NotificationGroup(msg("grazi.update.group"),
            NotificationDisplayType.STICKY_BALLOON, true)
    private val NOTIFICATION_GROUP_LANGUAGES = NotificationGroup(msg("grazi.languages.group"),
            NotificationDisplayType.STICKY_BALLOON, true)

    fun showInstallationMessage(project: Project) = NOTIFICATION_GROUP_INSTALL
            .createNotification(
                    msg("grazi.install.title"), msg("grazi.install.body"),
                    NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
            .notify(project)

    fun showUpdateMessage(project: Project) = NOTIFICATION_GROUP_UPDATE
            .createNotification(
                    msg("grazi.update.title", GraziPlugin.version), msg("grazi.update.body"),
                    NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER)
            .notify(project)

    fun showLanguagesMessage(project: Project) = NOTIFICATION_GROUP_LANGUAGES
            .createNotification(msg("grazi.languages.title"), msg("grazi.languages.body"), NotificationType.INFORMATION, null)
            .addAction(object : NotificationAction(msg("grazi.languages.action")) {
                override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                    LangDownloader.downloadMissingLanguages(project)
                }
            }).notify(project)
}
