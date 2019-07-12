package tanvd.grazi.ide.msg

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic

interface GraziAppLifecycle {
    companion object {
        val topic = Topic.create("grazi_app_lifecycle_topic", GraziAppLifecycle::class.java)
        val publisher: GraziAppLifecycle
            get() = ApplicationManager.getApplication().messageBus.syncPublisher(topic)
    }

    /** Initialize Grazi engine eagerly */
    fun init() {}

    /** Reset Grazi contexts */
    fun reset() {}

    /** Reset Grazi contexts and re-init eagerly */
    fun reInit() {
        reset()
        init()
    }
}
