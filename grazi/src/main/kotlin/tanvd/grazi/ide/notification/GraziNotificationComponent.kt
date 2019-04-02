package tanvd.grazi.ide.notification

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin

open class GraziNotificationComponent(private val project: Project) : ProjectComponent {
    override fun getComponentName(): String {
        return "GraziNotificationComponent"
    }

    override fun projectOpened() {
        if (GraziConfig.state.lastSeenVersion == null) {
            GraziConfig.state.lastSeenVersion = GraziPlugin.version
            Notification.showInstallationMessage(project)
        } else if (GraziPlugin.version != GraziConfig.state.lastSeenVersion) {
            GraziConfig.state.lastSeenVersion = GraziPlugin.version
            Notification.showUpdateMessage(project)
        }
    }
}
