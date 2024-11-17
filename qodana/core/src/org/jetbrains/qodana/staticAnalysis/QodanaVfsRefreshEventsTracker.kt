package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.RefreshQueueImpl
import com.intellij.platform.backend.observation.ActivityTracker
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.delay

class QodanaVfsRefreshEventsTracker : ActivityTracker {
  override val presentableName: String
    get() = "vfs-events"

  override suspend fun isInProgress(project: Project): Boolean {
    if (!PlatformUtils.isQodana()) {
      return false
    }
    return RefreshQueueImpl.isEventProcessingInProgress()
  }

  override suspend fun awaitConfiguration(project: Project) {
    while (isInProgress(project)) {
      delay(50)
    }
  }
}