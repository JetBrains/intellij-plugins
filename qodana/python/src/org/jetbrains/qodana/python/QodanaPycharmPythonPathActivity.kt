package org.jetbrains.qodana.python

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.ActivityKey
import com.intellij.platform.backend.observation.trackActivity
import com.intellij.pycharm.community.ide.impl.PySdkFromEnvironmentVariableConfigurator
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.QodanaLinterProjectActivity

class QodanaPycharmPythonPathActivity : QodanaLinterProjectActivity() {
  object Key : ActivityKey {
    override val presentableName: String = "PYCHARM_PYTHON_PATH environment update"
  }

  override suspend fun run(project: Project) {
    coroutineScope {
      project.trackActivity(Key) {
        val pycharmPythonPath = PySdkFromEnvironmentVariableConfigurator.getPycharmPythonPathProperty()
        if (pycharmPythonPath.isNullOrEmpty()) {
          return@trackActivity
        }

        val pycharmPythonPathSdk = withContext(QodanaDispatchers.Ui) {
          PySdkFromEnvironmentVariableConfigurator.findOrCreateSdkByPath(pycharmPythonPath)
        } ?: return@trackActivity
        val projectSdk = ProjectRootManager.getInstance(project).projectSdk

        fun setSdkForModules(modules: List<Module>) {
          launch(QodanaDispatchers.Default) {
            project.trackActivity(Key) {
              modules.forEach { module ->
                withContext(QodanaDispatchers.Ui) {
                  PySdkFromEnvironmentVariableConfigurator.setModuleSdk(module, projectSdk, pycharmPythonPathSdk, pycharmPythonPath)
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
}