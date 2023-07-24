package com.intellij.dts.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.PROJECT)
@State(name = "DtsZephyrSettings", storages = [Storage("dtsZephyrSettings.xml")])
class DtsZephyrSettings : PersistentStateComponent<DtsZephyrSettings> {
    companion object {
        fun of(project: Project): DtsZephyrSettings = project.service()
    }

    var rootPath: String? = null
    var arch: String? = null
    var board: String? = null

    override fun getState(): DtsZephyrSettings = this

    override fun loadState(state: DtsZephyrSettings) = XmlSerializerUtil.copyBean(state, this)
}