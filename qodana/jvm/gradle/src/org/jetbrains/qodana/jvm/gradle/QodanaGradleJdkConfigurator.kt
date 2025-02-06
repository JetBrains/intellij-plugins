package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.TaskCancellation
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.PlatformUtils
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.settings.GradleSettingsListener
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.jvm.java.QodanaConfigJdkService

private class QodanaGradleJdkConfigurator : GradleSettingsListener {
  override fun onProjectsLinked(settings: Collection<GradleProjectSettings>) {
    if (!PlatformUtils.isQodana()) return
    if (ApplicationManager.getApplication().isDispatchThread) {
      runWithModalProgressBlocking(
        ModalTaskOwner.guess(),
        QodanaBundle.message("progress.title.apply.jdk.configured.by.qodana.to.gradle.settings"),
        TaskCancellation.nonCancellable()
      ) {
        updateJdk(settings)
      }
    }
    else {
      runBlockingMaybeCancellable { updateJdk(settings) }
    }
  }

  private suspend fun updateJdk(settings: Collection<GradleProjectSettings>) {
    val jdk = service<QodanaConfigJdkService>().getJdk() ?: return
    for (projectSettings in settings) {
      projectSettings.gradleJvm = jdk.name
    }
  }
}
