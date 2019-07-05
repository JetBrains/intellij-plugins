package tanvd.grazi.ide

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.messages.Topic

interface GraziLifecycle {
    companion object {
        val topic = Topic.create("grazi_lifecycle_topic", GraziLifecycle::class.java)
        val publisher: GraziLifecycle
            get() = ApplicationManager.getApplication().messageBus.syncPublisher(topic)
    }

    /** Initialize Grazi engine eagerly */
    fun init()
    /** Reset Grazi contexts */
    fun reset()
    /** Reset Grazi contexts and re-init eagerly */
    fun reInit() {
        reset()
        init()

        ProjectManager.getInstance().openProjects.forEach {
            DaemonCodeAnalyzer.getInstance(it).restart()
        }
    }
}
