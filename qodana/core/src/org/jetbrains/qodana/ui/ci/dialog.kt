package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.project.Project
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.findQodanaConfigVirtualFile
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SetupCiDialogSource

internal fun showSetupCIDialogOrWizardWithYaml(
  project: Project,
  statsSource: SetupCiDialogSource
): Job {
  return project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
    QodanaPluginStatsCounterCollector.SETUP_CI_DIALOG_OPENED.log(project, statsSource)

    val isQodanaYamlPresent = project.findQodanaConfigVirtualFile() != null
    withContext(QodanaDispatchers.Ui) {
      val dialog = if (isQodanaYamlPresent) {
        SetupCIDialog(project)
      }
      else {
        EditYamlAndSetupCIWizardDialog.create(project)
      }
      dialog.show()
    }
  }
}
