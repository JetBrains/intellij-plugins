/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.statistic

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import training.util.trainerPluginConfigName

@State(name = "ActivityManager", storages = [Storage(value = trainerPluginConfigName)])
class ActivityManager: PersistentStateComponent<ActivityManager> {

  private var lastActivityTime: Long? = null

  override fun getState(): ActivityManager = this

  override fun loadState(persistedState: ActivityManager) {
    lastActivityTime = if (persistedState.lastActivityTime == null || persistedState.lastActivityTime == 0L)
      System.currentTimeMillis()
    else
      persistedState.lastActivityTime
  }

  companion object {
    val instance: ActivityManager
      get() = ServiceManager.getService(ActivityManager::class.java)
  }

}