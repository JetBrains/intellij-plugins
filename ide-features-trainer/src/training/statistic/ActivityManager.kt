package training.statistic

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "ActivityManager", storages = arrayOf(Storage(value = "ide-features-trainer.xml")))
class ActivityManager: PersistentStateComponent<ActivityManager> {

  var lastActivityTime: Long? = null

  override fun getState(): ActivityManager = this

  override fun loadState(persistedState: ActivityManager?) {
    if (persistedState == null) return
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