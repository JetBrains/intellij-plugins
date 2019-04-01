package tanvd.grazi.ide.notification

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin

open class GraziUpdateComponent(val project: Project) : ProjectComponent {
    override fun getComponentName(): String {
        return "GraziUpdateComponent"
    }

    override fun projectOpened() {
        if (GraziPlugin.version != GraziConfig.state.lastSeenVersion) {
            GraziConfig.state.lastSeenVersion = GraziPlugin.version
            Notification.showUpdate(project)
        }
    }
}
