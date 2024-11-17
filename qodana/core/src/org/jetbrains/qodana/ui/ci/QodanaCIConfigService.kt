package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.azure.AzurePipelinesCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.bitbucket.BitbucketCIFIleChecker
import org.jetbrains.qodana.ui.ci.providers.circleci.CircleCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.github.GitHubCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.gitlab.GitLabCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.jenkins.JenkinsCIFileChecker
import org.jetbrains.qodana.ui.ci.providers.space.SpaceAutomationCIFileChecker
import kotlin.io.path.Path

@State(name = "QodanaCIConfigService", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE)])
@Service(Service.Level.PROJECT)
class QodanaCIConfigService(project: Project, scope: CoroutineScope)
  : PersistentStateComponent<QodanaCIConfigService.State> {
  companion object {
    fun getInstance(project: Project): QodanaCIConfigService = project.service()
  }

  private val _presentCIFile: MutableStateFlow<CIFile.ExistingWithQodana?> = MutableStateFlow(null)
  val presentCIFile = _presentCIFile.asStateFlow()

  private val toCheck = mapOf(
    Pair(CIType.GITHUB, GitHubCIFileChecker(project, scope)),
    Pair(CIType.GITLAB, GitLabCIFileChecker(project, scope)),
    Pair(CIType.JENKINS, JenkinsCIFileChecker(project, scope)),
    Pair(CIType.AZURE, AzurePipelinesCIFileChecker(project, scope)),
    Pair(CIType.CIRCLECI, CircleCIFileChecker(project, scope)),
    Pair(CIType.SPACE, SpaceAutomationCIFileChecker (project, scope)),
    Pair(CIType.BITBUCKET, BitbucketCIFIleChecker(project, scope))
  )

  init {
    scope.launch(QodanaDispatchers.Default) {
      _presentCIFile.collectLatest { file ->
        when (file) {
          is CIFile.ExistingWithQodana -> {
            toCheck.values.map { it.ciFileFlow }.merge().filterNotNull().filterNot { it is CIFile.ExistingWithQodana }.filter {
              file.path.startsWith(it.path)
            }.collectLatest {
              updatePresentCIFile(null)
            }
          }
          null -> {
            toCheck.values.map { it.ciFileFlow }.merge().filterNotNull().filterIsInstance<CIFile.ExistingWithQodana>().collectLatest {
              updatePresentCIFile(it)
            }
          }
        }
      }
    }
  }

  private fun updatePresentCIFile(ciFile: CIFile.ExistingWithQodana?) {
    if (ciFile == null) {
      _presentCIFile.value = null
    } else {
      _presentCIFile.compareAndSet(null, ciFile)
    }
  }

  class State : BaseState() {
    var path by string()
    var ciType by enum<CIType>()
  }

  override fun getState(): State {
    val currentCIFIle = presentCIFile.value
    return State().apply {
      path = currentCIFIle?.path
      ciType = currentCIFIle?.ciFileChecker?.toCIType()
    }
  }

  override fun loadState(state: State) {
    val path = state.path ?: return
    val ciFileChecker = toCheck[state.ciType] ?: return
    val file = VirtualFileManager.getInstance().findFileByNioPath(Path(path))
    _presentCIFile.value = CIFile.ExistingWithQodana(path, ciFileChecker, file)
  }
}

enum class CIType {
  GITHUB,
  GITLAB,
  JENKINS,
  AZURE,
  CIRCLECI,
  SPACE,
  BITBUCKET
}

private fun CIFileChecker.toCIType(): CIType? {
  return when (this) {
    is GitHubCIFileChecker -> CIType.GITHUB
    is GitLabCIFileChecker -> CIType.GITLAB
    is JenkinsCIFileChecker -> CIType.JENKINS
    is AzurePipelinesCIFileChecker -> CIType.AZURE
    is CircleCIFileChecker -> CIType.CIRCLECI
    is SpaceAutomationCIFileChecker -> CIType.SPACE
    is BitbucketCIFIleChecker -> CIType.BITBUCKET
    else -> null
  }
}
