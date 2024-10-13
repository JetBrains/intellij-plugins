package org.jetbrains.qodana.ui.problemsView.viewModel

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.problemsView.QodanaGroupByModuleSupportService

data class QodanaProblemsViewState(
  val showBaselineProblems: Boolean = false,
  val groupBySeverity: Boolean = true,
  val groupByInspection: Boolean = true,
  val groupByModule: Boolean = QodanaGroupByModuleSupportService.getInstance().isSupported.value,
  val groupByDirectory: Boolean = true,
)

@Service(Service.Level.PROJECT)
@State(name = "QodanaProblemsViewState", storages = [(Storage(value = StoragePathMacros.WORKSPACE_FILE))])
class QodanaProblemsViewStateService(@Suppress("UNUSED_PARAMETER") project: Project, scope: CoroutineScope)
  : PersistentStateComponent<QodanaProblemsViewStateService.PersistedState> {
  companion object {
    fun getInstance(project: Project): QodanaProblemsViewStateService = project.service()
  }

  private val _problemsViewStateFlow = MutableStateFlow(QodanaProblemsViewState())
  val problemsViewStateFlow: StateFlow<QodanaProblemsViewState> = _problemsViewStateFlow.asStateFlow()

  init {
    scope.launch(QodanaDispatchers.Default) {
      QodanaGroupByModuleSupportService.getInstance().isSupported
        .drop(1)
        .filter { !it }
        .collect {
          updateProblemsViewState { it.copy(groupByModule = false) }
        }
    }
  }

  fun updateProblemsViewState(update: (QodanaProblemsViewState) -> QodanaProblemsViewState) {
    _problemsViewStateFlow.update {
      val isGroupByModuleSupported by lazy { QodanaGroupByModuleSupportService.getInstance().isSupported.value }
      val newState = update.invoke(it)

      newState.copy(groupByModule = newState.groupByModule && isGroupByModuleSupported)
    }
  }

  class PersistedState : BaseState() {
    var showBaselineProblems: Boolean by property(false)

    var groupBySeverity: Boolean by property(true)

    var groupByInspection: Boolean by property(true)

    var groupByModule: Boolean by property(QodanaGroupByModuleSupportService.getInstance().isSupported.value)

    var groupByDirectory: Boolean by property(true)
  }

  override fun getState(): PersistedState {
    val state = problemsViewStateFlow.value
    return PersistedState().apply {
      showBaselineProblems = state.showBaselineProblems
      groupBySeverity = state.groupBySeverity
      groupByInspection = state.groupByInspection
      groupByModule = state.groupByModule
      groupByDirectory = state.groupByDirectory
    }
  }

  override fun loadState(state: PersistedState) {
    updateProblemsViewState {
      QodanaProblemsViewState(
        showBaselineProblems = state.showBaselineProblems,
        groupBySeverity = state.groupBySeverity,
        groupByInspection = state.groupByInspection,
        groupByModule = state.groupByModule,
        groupByDirectory = state.groupByDirectory
      )
    }
  }
}