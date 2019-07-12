package tanvd.grazi.ide.notification

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin

open class GraziNotificationComponent : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        val state = GraziConfig.get()

        if (state.lastSeenVersion == null) {
            GraziConfig.update(state.copy(lastSeenVersion = GraziPlugin.version))
            Notification.showInstallationMessage(project)
        } else if (GraziPlugin.version != state.lastSeenVersion) {
            GraziConfig.update(state.copy(lastSeenVersion = GraziPlugin.version))
            Notification.showUpdateMessage(project)
        }
    }
}
