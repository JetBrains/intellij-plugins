package org.jetbrains.qodana.settings

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.refreshAndFindVirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.util.io.createParentDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.extensions.ConfigUpdateHandler
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createFile

@Service(Service.Level.PROJECT)
class QodanaConfigChangeService(val project: Project, val scope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): QodanaConfigChangeService = project.service()
  }

  private val updateConfigChannel = Channel<ConfigExcludeItem>()

  init {
    scope.launch(QodanaDispatchers.Default) {
      updateConfigChannel.consumeAsFlow().collect { data ->
        supervisorScope {
          launch exclude@ {
            val configFile = getConfigFile() ?: return@exclude
            ConfigUpdateHandler.excludeFromConfig(project, configFile, data.inspectionId, data.path)
          }
        }
      }
    }
  }

  suspend fun excludeData(data: ConfigExcludeItem) {
    updateConfigChannel.send(data)
  }

  @VisibleForTesting
  suspend fun getConfigFile(): VirtualFile? {
    val projectPath = project.guessProjectDir()?.toNioPath() ?: return null
    val configPath = QodanaYamlReader.defaultConfigPath(projectPath) ?: return createDefaultConfig(projectPath)
    return loadFile(configPath)
  }

  private fun loadFile(path: Path): VirtualFile? {
     return VirtualFileManager.getInstance().findFileByNioPath(path)
  }

  private suspend fun createDefaultConfig(projectPath: Path): VirtualFile? {
    return withContext(QodanaDispatchers.IO) {
      val file = projectPath.resolve(QODANA_YAML_CONFIG_FILENAME).createParentDirectories().createFile().refreshAndFindVirtualFile()
                 ?: return@withContext null
      val text = createDefaultConfigContent()
      writeAction {
        file.writeText(text)
      }
      return@withContext file
    }
  }

  suspend fun createDefaultConfigContent(): String {
    return DefaultQodanaYamlBuilder(project).build()
  }
}

class ConfigExcludeItem(val inspectionId: String?, initPath: String?) {
  val path: String? = if (initPath == "") null else initPath

  fun isRelatedToProblem(sarifProblem: SarifProblem): Boolean {
    return when {
      inspectionId != null && path != null -> sarifProblem.inspectionId == inspectionId && sarifProblem.relativePathToFile.startsWith(path)
      inspectionId == null && path != null -> sarifProblem.relativePathToFile.startsWith(path)
      inspectionId != null && path == null -> sarifProblem.inspectionId == inspectionId
      else -> true
    }
  }

  fun excludesPath(otherPath: Path, targetInspectionId: String? = null): Boolean {
    return (inspectionId == null || inspectionId == targetInspectionId) && (path == null || otherPath.startsWith(path))
  }

  fun isRelatedToPath(otherPath: String): Boolean {
    return path == null || path.startsWith(otherPath) || otherPath.startsWith(path)
  }

  override fun hashCode(): Int {
    return Objects.hash(inspectionId, path)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ConfigExcludeItem) return false

    return inspectionId == other.inspectionId && path == other.path
  }
}
