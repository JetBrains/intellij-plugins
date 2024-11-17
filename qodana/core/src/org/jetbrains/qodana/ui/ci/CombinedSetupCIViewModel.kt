package org.jetbrains.qodana.ui.ci
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Nls
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SetupCiProvider
import org.jetbrains.qodana.ui.ProjectVcsDataProvider
import org.jetbrains.qodana.ui.ci.providers.azure.SetupAzurePipelinesViewModel
import org.jetbrains.qodana.ui.ci.providers.bitbucket.SetupBitbucketCIViewModel
import org.jetbrains.qodana.ui.ci.providers.circleci.SetupCircleCIViewModel
import org.jetbrains.qodana.ui.ci.providers.github.SetupGitHubActionsViewModel
import org.jetbrains.qodana.ui.ci.providers.gitlab.SetupGitLabCIViewModel
import org.jetbrains.qodana.ui.ci.providers.jenkins.SetupJenkinsViewModel
import org.jetbrains.qodana.ui.ci.providers.space.SetupSpaceAutomationViewModel
import org.jetbrains.qodana.ui.ci.providers.teamcity.SetupTeamcityDslViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class CombinedSetupCIViewModel(
  private val project: Project,
  private val scope: CoroutineScope,
  private val projectVcsDataProvider: ProjectVcsDataProvider,
) {
  val allSetupCIProviders: List<SetupCIProvider> = createAllSetupCIProviders()

  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  private val finishProviderStateFlow: StateFlow<SetupCIFinishProvider?> = createFinishProviderStateFlow()

  val isFinishAvailableFlow: Flow<Boolean> = finishProviderStateFlow.map { it != null }

  private val _finishHappenedFlow = MutableSharedFlow<Unit>()
  val finishHappenedFlow = _finishHappenedFlow.asSharedFlow()

  val nextButtonTextFlow: Flow<@Nls String?> = uiState.map {
    when (it) {
      is UiState.ActivePanelProvider -> it.ciPanelProvider.nextButtonText
      else -> null
    }
  }


  sealed interface UiState {
    object Loading : UiState

    data class ActivePanelProvider(val ciPanelProvider: SetupCIProvider.Available) : UiState

    object Empty : UiState
  }

  init {
    scope.launch(QodanaDispatchers.Default) {
      val newUiState = firstCIProviderInProject()?.let { UiState.ActivePanelProvider(it) } ?: UiState.Empty
      _uiState.compareAndSet(UiState.Loading, newUiState)
    }
  }

  private fun createFinishProviderStateFlow(): StateFlow<SetupCIFinishProvider?> {
    return uiState.flatMapLatest { uiState ->
      val activePanelProviderUiState = uiState as? UiState.ActivePanelProvider ?: return@flatMapLatest flowOf(null)

      val finishProviderLoggingStatsFlow = activePanelProviderUiState.ciPanelProvider.viewModel.finishProviderFlow
        .map { finishProvider ->
          if (finishProvider == null) return@map null

          val loggingStatsWrapper = suspend {
            logSetupCiFinishedStats(activePanelProviderUiState.ciPanelProvider)
            finishProvider.invoke()
          }
          loggingStatsWrapper
        }
      finishProviderLoggingStatsFlow
    }.flowOn(QodanaDispatchers.Default).stateIn(scope, SharingStarted.Eagerly, null)
  }

  fun finish() {
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      val finishProvider = finishProviderStateFlow.value ?: return@launch
      _finishHappenedFlow.emit(Unit)
      finishProvider.invoke()
    }
  }

  private fun logSetupCiFinishedStats(provider: SetupCIProvider.Available) {
    val providerTypeStats = when(provider.viewModel) {
      is SetupGitHubActionsViewModel -> SetupCiProvider.GITHUB
      is SetupGitLabCIViewModel -> SetupCiProvider.GITLAB
      is SetupTeamcityDslViewModel -> SetupCiProvider.TEAMCITY
      is SetupJenkinsViewModel -> SetupCiProvider.JENKINS
      is SetupAzurePipelinesViewModel -> SetupCiProvider.AZURE
      is SetupCircleCIViewModel -> SetupCiProvider.CIRCLECI
      is SetupSpaceAutomationViewModel -> SetupCiProvider.SPACE
      is SetupBitbucketCIViewModel -> SetupCiProvider.BITBUCKET
      else -> return
    }
    QodanaPluginStatsCounterCollector.SETUP_CI_DIALOG_FINISHED.log(
      project,
      providerTypeStats
    )
  }

  fun setActiveCIPanelProvider(ciPanelProvider: SetupCIProvider.Available) {
    val oldUiState = _uiState.getAndUpdate {
      UiState.ActivePanelProvider(ciPanelProvider)
    }
    (oldUiState as? UiState.ActivePanelProvider)?.ciPanelProvider?.viewModel?.unselected()
  }

  private fun createAllSetupCIProviders(): List<SetupCIProvider> {
    return SetupCIProviderFactory.EP.extensionList.mapNotNull { it.createSetupCIProvider(project, scope, projectVcsDataProvider) }
  }

  private suspend fun firstCIProviderInProject(): SetupCIProvider.Available? {
    val availableSetupCIProviders = allSetupCIProviders.filterIsInstance<SetupCIProvider.Available>()
    if (availableSetupCIProviders.isEmpty()) return null

    val matchingCIProvider = availableSetupCIProviders
      .map { ciPanelProvider ->
        suspend {
          if (ciPanelProvider.viewModel.isCIPresentInProject()) ciPanelProvider else null
        }.asFlow()
      }
      .merge()
      .filterNotNull()
      .firstOrNull()

    return matchingCIProvider ?: availableSetupCIProviders.first()
  }
}