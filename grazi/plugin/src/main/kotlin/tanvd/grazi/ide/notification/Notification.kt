package tanvd.grazi.ide.notification


import com.intellij.notification.*
import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.language.Lang
import tanvd.grazi.remote.GraziRemote
import tanvd.grazi.utils.joinToStringWithOxfordComma

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

    fun showLanguagesMessage(project: Project) {
        val langs = GraziConfig.get().missedLanguages
        val s = if (langs.size > 1) "s" else ""
        NOTIFICATION_GROUP_LANGUAGES
                .createNotification(msg("grazi.languages.title", s),
                        msg("grazi.languages.body", langs.toList().joinToStringWithOxfordComma()),
                        NotificationType.WARNING, null)
                .addAction(object : NotificationAction(msg("grazi.languages.action.download", s)) {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        GraziRemote.downloadMissing(project)
                        notification.hideBalloon()
                    }
                })
                .addAction(object : NotificationAction(msg("grazi.languages.action.disable", s)) {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        GraziConfig.update { state ->
                            val native = if (state.nativeLanguage.jLanguage == null) {
                                Lang.AMERICAN_ENGLISH
                            } else {
                                state.nativeLanguage
                            }

                            val enabledLanguages = state.enabledLanguages - state.missedLanguages
                            state.copy(enabledLanguages = enabledLanguages, nativeLanguage = native,
                                    enabledLanguagesAvailable = enabledLanguages.filter { it.jLanguage != null }.toSet())
                        }
                        notification.hideBalloon()
                    }
                })
                .notify(project)
    }
}
