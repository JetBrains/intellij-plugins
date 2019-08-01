package tanvd.grazi.ide.notification

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin

open class GraziNotificationComponent : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        GraziConfig.update {
            when {
                it.lastSeenVersion == null -> Notification.showInstallationMessage(project)
                GraziPlugin.version != it.lastSeenVersion -> Notification.showUpdateMessage(project)
                it.hasMissedLanguages() -> Notification.showLanguagesMessage(project)
            }
            it.copy(lastSeenVersion = GraziPlugin.version)
        }
    }
}
