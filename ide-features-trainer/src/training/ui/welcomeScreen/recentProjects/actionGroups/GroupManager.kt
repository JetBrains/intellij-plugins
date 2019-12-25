// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.ui.welcomeScreen.recentProjects.actionGroups

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.xmlb.annotations.XMap
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.DefaultProjectsActionGroup
import training.ui.welcomeScreen.recentProjects.actionGroups.commonActions.TutorialsActionGroup
import training.util.trainerPluginConfigName

@State(name = "GroupManager", storages = [Storage(value = trainerPluginConfigName)])
class GroupManager : PersistentStateComponent<GroupManager> {

  companion object {
    @JvmStatic
    val instance: GroupManager
      get() = service()
  }

  @XMap(keyAttributeName = "name", valueAttributeName = "expanded")
  private var expandedByName: MutableMap<String, Boolean> = mutableMapOf()

  var showTutorialsOnWelcomeFrame: Boolean = Registry.`is`("ideFeaturesTrainer.welcomeScreen.tutorialsTree")

  val registeredGroups: List<CommonActionGroup> = listOf(
    TutorialsActionGroup(),
    DefaultProjectsActionGroup()
  )

  override fun getState(): GroupManager {
    registeredGroups.forEach { expandedByName[it.name] = it.isExpanded }
    return this
  }

  @Synchronized
  override fun loadState(state: GroupManager) {
    showTutorialsOnWelcomeFrame = state.showTutorialsOnWelcomeFrame
    expandedByName = state.expandedByName
    registeredGroups.forEach {
      val expanded = state.expandedByName[it.name] ?: it.isExpanded
      it.apply { isExpanded = expanded }
    }
  }

}