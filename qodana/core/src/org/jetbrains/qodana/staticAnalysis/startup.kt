package org.jetbrains.qodana.staticAnalysis

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.observation.ActivityKey
import com.intellij.platform.backend.observation.trackActivity
import kotlinx.coroutines.Job

class QodanaLinterBackgroundStartupActivity : QodanaLinterProjectActivity() {
  override suspend fun run(project: Project) {
    try {
      logger<QodanaLinterBackgroundStartupActivity>().info("Executed Qodana backgroundPostStartupActivity")
    }
    finally {
      project.service<QodanaBackgroundStartupActivityFinished>().isFinished.complete()
    }
  }

}

class QodanaAwaitBackgroundStartupActivity : QodanaLinterProjectActivity() {
  private object Key : ActivityKey {
    override val presentableName: String = "background activities start"
  }

  override suspend fun run(project: Project) {
    project.trackActivity(Key) {
      project.service<QodanaBackgroundStartupActivityFinished>().isFinished.join()
    }
  }
}

@Service(Service.Level.PROJECT)
private class QodanaBackgroundStartupActivityFinished() {
  val isFinished = Job()
}