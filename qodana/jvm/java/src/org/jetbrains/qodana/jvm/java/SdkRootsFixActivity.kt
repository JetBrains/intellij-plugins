package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.impl.UnknownSdkActivityFinishedService
import com.intellij.platform.backend.observation.ActivityKey
import com.intellij.platform.backend.observation.trackActivity
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity

class SdkRootsFixActivity : QodanaLinterProjectActivity() {
  private object Key : ActivityKey {
    override val presentableName: String = "sdk-roots"
  }

  override suspend fun run(project: Project) {
    project.trackActivity(Key) {
      UnknownSdkActivityFinishedService.getInstance(project).await()
      setupSdksRootPaths()
    }
  }

  private fun setupSdksRootPaths() {
    ProjectJdkTable.getInstance().allJdks
      .forEach { sdk ->
        val sdkType = sdk.sdkType as? JavaSdk ?: return@forEach
        thisLogger().info("Set resolve roots for SDK $sdk")
        sdkType.setupSdkPaths(sdk)
      }
  }
}