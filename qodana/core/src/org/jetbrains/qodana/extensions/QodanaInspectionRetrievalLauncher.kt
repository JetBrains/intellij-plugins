package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import java.util.concurrent.atomic.AtomicBoolean

interface QodanaInspectionRetrievalLauncher {
  companion object {
    private val EP_NAME: ExtensionPointName<QodanaInspectionRetrievalLauncher> =
      ExtensionPointName.create("org.intellij.qodana.qodanaInspectionRetrievalLauncher")

    suspend fun launchInspectionRetrieval(project: Project) {
      for (e in EP_NAME.extensionList) {
        e.launchInspectionRetrieval(project)
      }
      isInitialized.set(true)
    }

    private val isInitialized: AtomicBoolean = AtomicBoolean(false)

    fun isInitialized(): Boolean = isInitialized.get()
  }

  suspend fun launchInspectionRetrieval(project: Project)
}