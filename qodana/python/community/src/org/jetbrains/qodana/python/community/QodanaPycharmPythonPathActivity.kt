package org.jetbrains.qodana.python.community

import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.observation.ActivityKey
import com.intellij.platform.backend.observation.trackActivity
import com.jetbrains.python.sdk.PySdkFromEnvironmentVariable
import com.jetbrains.python.sdk.getOrLog
import com.jetbrains.python.statistics.modules
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity

private const val QODANA_PYTHON_PATH_ENV = "QODANA_PYTHON_PATH"
private const val QODANA_PYTHON_PATH_PROPERTY = "qodana.python.path"

private val logger = fileLogger()

internal class QodanaPycharmPythonPathActivity : QodanaLinterProjectActivity() {
  private object Key : ActivityKey {
    override val presentableName: String = "$QODANA_PYTHON_PATH_ENV environment update"
  }

  override suspend fun run(project: Project) {
    coroutineScope {
      project.trackActivity(Key) {
        val modules = project.modules
        if (configureSdkFromQodanaPythonPath(project, modules) != QodanaPythonPathConfigurationResult.Success) {
          return@trackActivity
        }

        fun setSdkForModules(modules: Array<Module>) {
          this@coroutineScope.launch(QodanaDispatchers.Default) {
            project.trackActivity(Key) {
              configureSdkFromQodanaPythonPath(project, modules)
            }
          }
        }

        val currentModules = ModuleManager.getInstance(project).modules
        val listener = object : ModuleListener {
          override fun modulesAdded(project: Project, modules: List<Module>) {
            setSdkForModules(modules.toTypedArray())
          }
        }
        project.messageBus.connect(this@coroutineScope).subscribe(ModuleListener.TOPIC, listener)
        setSdkForModules(currentModules)
      }
      awaitCancellation()
    }
  }
}

internal enum class QodanaPythonPathConfigurationResult {
  NoPath,
  Failure,
  Success,
}

internal suspend fun configureSdkFromQodanaPythonPath(
  project: Project,
  modules: Array<Module>,
): QodanaPythonPathConfigurationResult {
  val variable = PySdkFromEnvironmentVariable.create(
    project,
    propertyName = QODANA_PYTHON_PATH_PROPERTY,
    envVarName = QODANA_PYTHON_PATH_ENV,
  ) ?: return QodanaPythonPathConfigurationResult.NoPath

  val sdk = variable.getOrLog(logger) ?: return QodanaPythonPathConfigurationResult.Failure
  sdk.configureSdkForModulesLogIfError(logger, modules)
  return QodanaPythonPathConfigurationResult.Success
}
