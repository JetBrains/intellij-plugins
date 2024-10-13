package org.jetbrains.qodana.ui.link

import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.project.CloudOrganizationPrimaryData
import org.jetbrains.qodana.cloud.project.CloudProjectData
import org.jetbrains.qodana.cloud.project.CloudProjectPrimaryData
import org.jetbrains.qodana.cloud.project.CloudProjectProperties
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudRequestParameters
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers

private const val ALLOWED_CHAR_FOR_REPLACEMENT = '-'
private const val ALLOWED_CHARS_REGEX_STRING = "a-zA-Z0-9 .${ALLOWED_CHAR_FOR_REPLACEMENT}"

class CreateProjectDialogViewModel(
  private val scope: CoroutineScope,
  private val organizationsFilter: (QDCloudSchema.Organization) -> Boolean,
  private val onProjectCreated: (CloudProjectData) -> Unit
) {
  private val projectNameRegex = Regex("^[$ALLOWED_CHARS_REGEX_STRING]*\$")
  private val notAllowedCharsRegex = Regex("[^$ALLOWED_CHARS_REGEX_STRING]")

  private val _dialogDataFlow: MutableStateFlow<DialogData?> = MutableStateFlow(null)
  val dialogDataFlow = _dialogDataFlow.asStateFlow()

  private val _teamsListFlow: MutableStateFlow<List<QodanaCloudTeamResponseWrapper>> = MutableStateFlow(emptyList())
  val teamsListFlow = _teamsListFlow.asStateFlow()

  private val _selectedItemFlow: MutableStateFlow<QodanaCloudTeamResponseWrapper?> = MutableStateFlow(null)
  val selectedItemFlow = _selectedItemFlow.asStateFlow()

  private val _elementsStatusFlow: MutableSharedFlow<ElementStatus> = MutableSharedFlow(replay = 3)
  val elementStatusFlow = _elementsStatusFlow.asSharedFlow()

  private val _projectNameFlow = MutableStateFlow("")
  val projectNameFlow = _projectNameFlow.asStateFlow()

  private val _finishFlow: MutableSharedFlow<Unit> = MutableSharedFlow()
  val finishFlow = _finishFlow.asSharedFlow()

  private val _errorFlow: MutableSharedFlow<CreateProjectError?> = MutableSharedFlow()
  val errorFlow = merge(_errorFlow.asSharedFlow(), createProjectNameErrorMessageFlow())

  init {
    scope.launch(QodanaDispatchers.Default) {
      QodanaCloudStateService.getInstance().userState.collectLatest { userState ->
        coroutineScope {
          when (userState) {
            is UserState.Authorized -> updateInfo(this, userState)
            else -> _dialogDataFlow.value = null
          }
          awaitCancellation()
        }
      }
    }
  }

  private suspend fun updateInfo(myScope: CoroutineScope, authorized: UserState.Authorized) {
    val organizationsLoader = OrganizationsLoader(myScope, authorized, organizationsFilter).also { it.load() }
    _dialogDataFlow.value = DialogData(organizationsLoader)
    checkLoaderState(organizationsLoader)
  }

  private suspend fun checkLoaderState(organizationsLoader: OrganizationsLoader) {
    organizationsLoader.loaderState.collect { loaderState ->
      when (loaderState) {
        is OrganizationsLoader.LoaderState.Loaded -> {
          _elementsStatusFlow.emit(ElementStatus.OkButtonStatus(true))
          _elementsStatusFlow.emit(ElementStatus.LoadingTeamsIconStatus(false))

          when (val teamsResponse = loaderState.teamsResponse) {
            is QDCloudResponse.Success -> {
              val teams = teamsResponse.value
              if (teams.isEmpty()) {
                _errorFlow.emit(CreateProjectError.LoadTeamsError(QodanaBundle.message("qodana.create.cloud.project.dialog.errors.no.teams")))
                return@collect
              }
              _teamsListFlow.value = teams
            }
            is QDCloudResponse.Error.ResponseFailure -> {
              _errorFlow.emit(
                CreateProjectError.LoadTeamsError(
                  QodanaBundle.message("qodana.create.cloud.project.dialog.errors.failed.loading.teams", teamsResponse.errorMessage)
                )
              )
            }
            is QDCloudResponse.Error.Offline -> {
              _errorFlow.emit(CreateProjectError.LoadTeamsError(QodanaBundle.message("qodana.cloud.offline")))
            }
          }
        }
        else -> {
          _elementsStatusFlow.emit(ElementStatus.LoadingTeamsIconStatus(true))
          _elementsStatusFlow.emit(ElementStatus.OkButtonStatus(false))
        }
      }
    }
  }

  fun setProjectName(newPath: String) {
    _projectNameFlow.value = newPath
  }

  fun setSelectedTeam(info: QodanaCloudTeamResponseWrapper) {
    _selectedItemFlow.value = info
  }

  private fun createProjectNameErrorMessageFlow(): SharedFlow<CreateProjectError.ProjectNameError?> {
    return _projectNameFlow
      .map { CreateProjectError.ProjectNameError(validateProjectName(it)) }
      .distinctUntilChanged()
      .flowOn(QodanaDispatchers.Default)
      .shareIn(scope, SharingStarted.Eagerly, replay = 1)
  }

  private fun validateProjectName(projectName: String): @NlsContexts.DialogMessage String? {
    return when {
      projectName.isEmpty() -> QodanaBundle.message("qodana.create.cloud.project.dialog.errors.project.name.empty")
      (!projectNameRegex.matches(projectName)) -> QodanaBundle.message("qodana.create.cloud.project.dialog.errors.project.name.invalid")
      else -> return null
    }
  }

  fun escapeProjectNameInvalidChars(projectName: String): String {
    return projectName.replace(notAllowedCharsRegex, ALLOWED_CHAR_FOR_REPLACEMENT.toString())
  }

  fun createProject() {
    scope.launch(QodanaDispatchers.Ui) {
      _errorFlow.emit(CreateProjectError.ProjectCreateError(null))
      val authorized = QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized ?: kotlin.run {
        _errorFlow.emit(CreateProjectError.ProjectCreateError(QodanaBundle.message("qodana.create.cloud.project.dialog.errors.not.logged.in")))
        return@launch
      }
      val selectedItem: QodanaCloudTeamResponseWrapper = _selectedItemFlow.value ?: return@launch
      val projectName = _projectNameFlow.value
      _elementsStatusFlow.emit(ElementStatus.OkButtonStatus(false))

      _elementsStatusFlow.emit(ElementStatus.CreatingProjectIconStatus(true))


      val cloudProjectDataResponse = createProject(authorized, selectedItem, projectName)
      _elementsStatusFlow.emit(ElementStatus.OkButtonStatus(true))

      _elementsStatusFlow.emit(ElementStatus.CreatingProjectIconStatus(false))

      when(cloudProjectDataResponse) {
        is QDCloudResponse.Success -> {
          _finishFlow.emit(Unit)
          onProjectCreated.invoke(cloudProjectDataResponse.value)
        }
        is QDCloudResponse.Error.ResponseFailure -> {
          _errorFlow.emit(CreateProjectError.ProjectCreateError(
            QodanaBundle.message("qodana.create.cloud.project.dialog.errors.creation.failed", cloudProjectDataResponse.errorMessage)))
        }
        is QDCloudResponse.Error.Offline -> {
          _errorFlow.emit(CreateProjectError.ProjectCreateError(QodanaBundle.message("qodana.cloud.offline")))
        }
      }
    }
  }

  fun refreshTeams(dialogData: DialogData) {
    scope.launch {
      _errorFlow.emit(CreateProjectError.LoadTeamsError(null))
      _teamsListFlow.emit(emptyList())
      dialogData.organizationsLoader.load()
    }
  }

  private suspend fun createProject(
    authorized: UserState.Authorized,
    selectedTeam: QodanaCloudTeamResponseWrapper,
    projectName: String
  ): QDCloudResponse<CloudProjectData> {
    return qodanaCloudResponse {
      val project = authorized.userApi().value()
        .createProjectInTeam(selectedTeam.team.id, projectName).value()
      val teamOrganization = selectedTeam.organization
      @Suppress("HardCodedStringLiteral")
      CloudProjectData(
        CloudProjectPrimaryData(project.id, CloudOrganizationPrimaryData(teamOrganization.id)),
        CloudProjectProperties(project.name)
      )
    }
  }

  class DialogData(val organizationsLoader: OrganizationsLoader)

  sealed class CreateProjectError(val message: @NlsContexts.DialogMessage String?) {
    class ProjectNameError(message: @NlsContexts.DialogMessage String?) : CreateProjectError(message)

    class ProjectCreateError(message: @NlsContexts.DialogMessage String?) : CreateProjectError(message)

    class LoadTeamsError(message: @NlsContexts.DialogMessage String?) : CreateProjectError(message)
  }

  sealed interface ElementStatus {
    class OkButtonStatus(val isEnabled: Boolean) : ElementStatus

    class CreatingProjectIconStatus(val isVisible: Boolean) : ElementStatus

    class LoadingTeamsIconStatus(val isVisible: Boolean) : ElementStatus
  }
}

class QodanaCloudTeamResponseWrapper(
  val team: QDCloudSchema.Team,
  val organization: QDCloudSchema.Organization
)

class OrganizationsLoader(
  private val scope: CoroutineScope,
  private val authorized: UserState.Authorized,
  private val organizationsFilter: (QDCloudSchema.Organization) -> Boolean
) {
  private val _loaderState = MutableStateFlow<LoaderState?>(null)
  val loaderState = _loaderState.asStateFlow()

  fun load() {
    scope.launch(QodanaDispatchers.Default) {
      _loaderState.update { currentState ->
        when(currentState) {
          is LoaderState.Loaded -> LoaderState.Loading
          LoaderState.Loading -> return@launch
          null -> LoaderState.Loading
        }
      }
      val teams = qodanaCloudResponse {
        val api = authorized.userApi().value()

        api.getOrganizations().value().filter(organizationsFilter).map { organization ->
          async {
            api.getTeams(organization.id, QDCloudRequestParameters.Paginated(offset = 0, limit = Int.MAX_VALUE)).value().items.map {
              QodanaCloudTeamResponseWrapper(it, organization)
            }
          }
        }.awaitAll().flatten()
      }
      _loaderState.compareAndSet(LoaderState.Loading, LoaderState.Loaded(teams))
    }
  }

  sealed interface LoaderState {
    object Loading : LoaderState

    class Loaded(val teamsResponse: QDCloudResponse<List<QodanaCloudTeamResponseWrapper>>) : LoaderState
  }
}