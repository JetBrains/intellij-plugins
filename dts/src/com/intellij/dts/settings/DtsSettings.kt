package com.intellij.dts.settings

import com.intellij.dts.settings.DtsSettings.ChangeListener.Companion.TOPIC
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
@State(name = "com.intellij.dts.settings.DtsSettings", storages = [Storage("dtsSettings.xml")])
class DtsSettings(
  private val project: Project,
  private val parentScope: CoroutineScope,
) : PersistentStateComponent<DtsSettings.State> {
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

  /**
   * The path to the zephyr board directory.
   */
  val zephyrBoard: String?
    get() = state.zephyrBoard.ifBlank { null }

  /**
   * Whether to load zephyr settings from cmake. If set to true all zephyr
   * settings will be overwritten by cmake.
   */
  val zephyrCMakeSync: Boolean
    get() = state.zephyrCMakeSync

  override fun getState(): State = state.copy()

  override fun loadState(state: State) = XmlSerializerUtil.copyBean(state, this.state)

  @Synchronized
  fun update(block: State.() -> Unit) {
    block(state)

    parentScope.launch {
      readAction {
        if (project.isDisposed) return@readAction
        project.messageBus.syncPublisher(ChangeListener.TOPIC).settingsChanged(this@DtsSettings)
      }
    }
  }

  /**
   * Implementations can subscribe to the [TOPIC] to be notified if dts
   * settings are changed.
   *
   * There are no guaranties whether the callback will be invoked on the EDT
   * or a background thread.
   */
  fun interface ChangeListener {
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
