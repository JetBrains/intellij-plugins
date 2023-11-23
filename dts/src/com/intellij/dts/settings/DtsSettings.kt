package com.intellij.dts.settings

import com.intellij.dts.zephyr.DtsZephyrBoard
import com.intellij.openapi.application.ApplicationManager
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

  val zephyrBoard: DtsZephyrBoard?
    get() = state.zephyrBoard.ifBlank { null }?.let { DtsZephyrBoard(it) }

  /**
   * Whether to load zephyr settings from cmake. If set to true all zephyr
   * settings will be overwritten by cmake.
   */
  val zephyrCMakeSync: Boolean
    get() = state.zephyrCMakeSync

  override fun getState(): State = state

  override fun loadState(state: State) = XmlSerializerUtil.copyBean(state, this.state)

  @Synchronized
  fun update(block: State.() -> Unit) {
    ApplicationManager.getApplication().invokeLater {
      block(state)
      project.messageBus.syncPublisher(ChangeListener.TOPIC).settingsChanged(this)
    }
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
    var zephyrCMakeSync: Boolean = true,
  )
}
