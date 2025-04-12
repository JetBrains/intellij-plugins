package org.jetbrains.qodana.python.community

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.ActivityKey
import com.intellij.platform.backend.observation.trackActivity
import com.jetbrains.python.sdk.PySdkFromEnvironmentVariable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity

private const val QODANA_PYTHON_PATH_ENV = "QODANA_PYTHON_PATH"
private const val QODANA_PYTHON_PATH_PROPERTY = "qodana.python.path"

class QodanaPycharmPythonPathActivity : QodanaLinterProjectActivity() {
  object Key : ActivityKey {
    override val presentableName: String = "$QODANA_PYTHON_PATH_ENV environment update"
  }

  override suspend fun run(project: Project) {
    coroutineScope {
      project.trackActivity(Key) {
        val pycharmPythonPath = pythonPath()
        if (pycharmPythonPath.isNullOrEmpty()) {
          return@trackActivity
        }

        val pycharmPythonPathSdk = withContext(QodanaDispatchers.Ui) {
          PySdkFromEnvironmentVariable.findOrCreateSdkByPath(pycharmPythonPath)
        } ?: return@trackActivity
        val projectSdk = ProjectRootManager.getInstance(project).projectSdk

        fun setSdkForModules(modules: List<Module>) {
          launch(QodanaDispatchers.Default) {
            project.trackActivity(Key) {
              modules.forEach { module ->
                withContext(QodanaDispatchers.Ui) {
                  PySdkFromEnvironmentVariable.setModuleSdk(module, projectSdk, pycharmPythonPathSdk, pycharmPythonPath)
                }
              }
            }
          }
        }

        val currentModules = ModuleManager.getInstance(project).modules
        val listener = object : ModuleListener {
          override fun modulesAdded(project: Project, modules: List<Module>) {
            setSdkForModules(modules)
          }
        }
        project.messageBus.connect(this@coroutineScope).subscribe(ModuleListener.TOPIC, listener)
        setSdkForModules(currentModules.toList())
      }
      awaitCancellation()
    }
  }

  private fun pythonPath(): String? {
    return System.getenv(QODANA_PYTHON_PATH_ENV)
           ?: System.getProperty(QODANA_PYTHON_PATH_PROPERTY)
           ?: PySdkFromEnvironmentVariable.getPycharmPythonPathProperty()
  }
}