package training.ui.welcomeScreen.recentProjects.actionGroups

import com.intellij.openapi.components.*
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.DefaultProjectsActionGroup
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.TutorialsActionGroup
import training.util.trainerPluginConfigName

@State(name = "GroupManager", storages = [Storage(value = trainerPluginConfigName)])
class GroupManager : PersistentStateComponent<GroupManager> {

  private val stateLock = Any()

  companion object {
    @JvmStatic
    val instance: GroupManager
      get() = service()
  }

  var expandedByName: MutableMap<String, Boolean> = mutableMapOf()

  val registeredGroups: List<CommonActionGroup> = listOf(
    DefaultProjectsActionGroup().apply { val expanded = expandedByName[name] ?: true; isExpanded = expanded },
    TutorialsActionGroup().apply { val expanded = expandedByName[name] ?: true; isExpanded = expanded }
  )

  override fun getState(): GroupManager {
    registeredGroups.forEach { expandedByName[it.name] = it.isExpanded }
    return this
  }

  override fun loadState(state: GroupManager) {
    synchronized(stateLock) {
      expandedByName = state.expandedByName
      registeredGroups.forEach { val expanded = state.expandedByName[it.name] ?: true; it.apply { isExpanded = expanded } }
    }
  }

}