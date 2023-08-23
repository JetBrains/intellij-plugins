package com.intellij.dts.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(name = "com.intellij.dts.settings.DtsSettings", storages = [Storage("dtsSettings.xml")])
class DtsSettings : PersistentStateComponent<DtsSettings.State> {
    companion object {
        fun of(project: Project): DtsSettings = project.service()
    }

    private val state = State("", "", "")

    /**
     * The path to the zephyr root. Also called the Zephyr base in the
     * settings.
     *
     * If empty the path should be detected automatically.
     */
    val zephyrRoot: String?
        get() = state.zephyrRoot.nullIfBlank()

    val zephyrArch: String?
        get() = state.zephyrArch.nullIfBlank()

    val zephyrBoard: String?
        get() = state.zephyrBoard.nullIfBlank()

    override fun getState(): State = state

    override fun loadState(state: State) = XmlSerializerUtil.copyBean(state, this.state)

    @Synchronized
    fun update(block: State.() -> Unit) {
        block(state)
    }

    data class State(
        var zephyrRoot: String = "",
        var zephyrArch: String = "",
        var zephyrBoard: String = "",
    )
}

private fun String.nullIfBlank() = this.ifBlank { null }