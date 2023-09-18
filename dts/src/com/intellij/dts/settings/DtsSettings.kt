package com.intellij.dts.settings

import com.intellij.dts.zephyr.ZephyrBoard
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(name = "com.intellij.dts.settings.DtsSettings", storages = [Storage("dtsSettings.xml")])
class DtsSettings(private val project: Project) : PersistentStateComponent<DtsSettings.State> {
    companion object {
        fun of(project: Project): DtsSettings = project.service()
    }

    private val state = State()

    /**
     * The path to the zephyr root. Also called the Zephyr base in the
     * settings.
     *
     * If empty the path should be detected automatically.
     */
    val zephyrRoot: String?
        get() = state.zephyrRoot.ifBlank { null }

    val zephyrBoard: ZephyrBoard?
        get() = ZephyrBoard.unmarshal(state.zephyrBoard)

    override fun getState(): State = state

    override fun loadState(state: State) = XmlSerializerUtil.copyBean(state, this.state)

    @Synchronized
    fun update(block: State.() -> Unit) {
        val publisher = project.messageBus.syncPublisher(ChangeListener.TOPIC)
        block(state)
        publisher.settingsChanged(this)
    }

    interface ChangeListener {
        companion object {
            @Topic.ProjectLevel
            @JvmField
            val TOPIC = Topic("DtsSettingsChanged", ChangeListener::class.java, Topic.BroadcastDirection.NONE)
        }

        fun settingsChanged(settings: DtsSettings)
    }

    data class State(
        var zephyrRoot: String = "",
        var zephyrBoard: String = "",
    )
}
