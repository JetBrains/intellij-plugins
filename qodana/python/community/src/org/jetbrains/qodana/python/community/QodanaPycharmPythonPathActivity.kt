package org.jetbrains.qodana.python.community

import com.intellij.openapi.application.readAction
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

internal class QodanaPycharmPythonPathActivity : QodanaLinterProjectActivity() {
  private object Key : ActivityKey {
    override val presentableName: String = "$QODANA_PYTHON_PATH_ENV environment update"
  }

  private companion object {
    val logger = fileLogger()
  }

  override suspend fun run(project: Project) {
    coroutineScope {
      project.trackActivity(Key) {
        val variable =
          PySdkFromEnvironmentVariable.create(project, propertyName = QODANA_PYTHON_PATH_PROPERTY, envVarName = QODANA_PYTHON_PATH_ENV)
            ?.getOrLog(logger)
          ?: return@trackActivity

        suspend fun configureSdkForModules(modules: List<Module>) {
          variable.configureSdkForModulesLogIfError(logger, modules.toTypedArray())
        }

        val modules = readAction { project.modules.toList() }
        configureSdkForModules(modules)

        fun setSdkForModules(modules: List<Module>) {
          this@coroutineScope.launch(QodanaDispatchers.Default) {
            project.trackActivity(Key) {
              configureSdkForModules(modules)
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