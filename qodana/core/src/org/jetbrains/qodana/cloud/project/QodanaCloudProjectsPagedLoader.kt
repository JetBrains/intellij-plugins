package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudRequestParameters
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.extensions.RepositoryInfoProvider
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class QodanaCloudProjectsPagedLoader(
  private val project: Project,
  private val scope: CoroutineScope,
  private val authorized: UserState.Authorized,
  private val filterAppliedFlow: StateFlow<Boolean>,
  private val pageSize: Int = 15,
) {
  class TeamData(
    val id: String,
    val organizationId: String,
    @NlsSafe val name: String?,
    val projectsCount: Int,
    val membersCount: Int
  ) {
    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean = id == (other as? TeamData)?.id
  }

  class ProjectData(
    val id: String,
    val team: TeamData,
    @NlsSafe val name: String?,
    val problemsCount: Int?,
    val baselineProblemsCount: Int?,
    @NlsSafe val branch: String?,
    val lastRunDate: Date?,
    val vcsUrl: String?
  ) {
    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean = id == (other as? ProjectData)?.id
  }

  private enum class LoaderRequest {
    LOAD_MORE, RESET
  }

  private data class LoaderState(
    val teams: List<TeamData> = emptyList(),
    val currentTeamIndex: Int = 0,
    val currentProjectPageInTeamOffset: Int = 0,
    val hasMorePages: Boolean = true,
    val latestPageResponse: QDCloudResponse<List<ProjectData>>? = null
  ) {
    val isFinished: Boolean
      get() = !hasMorePages || latestPageResponse is QDCloudResponse.Error
  }

  private val requestsChannel = Channel<LoaderRequest>()

  private val _isLoadingStateFlow = MutableStateFlow(false)
  val isLoadingStateFlow = _isLoadingStateFlow.asStateFlow()

  val latestPageResponseFlow: SharedFlow<QDCloudResponse<List<ProjectData>>?> = createLatestPageResponseFlow()

  init {
    reset()
  }

  fun loadMore() {
    scope.launch(QodanaDispatchers.Default) {
      latestPageResponseFlow.first()
      requestsChannel.trySend(LoaderRequest.LOAD_MORE)
    }
  }

  fun reset() {
    scope.launch(QodanaDispatchers.Default) {
      requestsChannel.send(LoaderRequest.RESET)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun createLatestPageResponseFlow(): SharedFlow<QDCloudResponse<List<ProjectData>>?> {
    val loaderState: AtomicReference<LoaderState?> = AtomicReference(null)
    return filterAppliedFlow.transformLatest {
      emit(null)
      loaderState.set(null)
      emitAll(createInnerFlow(it, loaderState))
    }
      .flowOn(QodanaDispatchers.Default)
      .shareIn(scope, SharingStarted.Eagerly, replay = 1)
  }

  private suspend fun createInnerFlow(isFilterApplied: Boolean, loaderState: AtomicReference<LoaderState?>): Flow<QDCloudResponse<List<ProjectData>>?> {
   return requestsChannel.receiveAsFlow()
     .onEach { request ->
       val currentLoaderState = loaderState.get()
       loaderState.set(computeNewLoaderState(currentLoaderState, request, isFilterApplied))
     }
     .map {
       loaderState.get()?.latestPageResponse
     }
     .distinctUntilChanged()
  }

  private suspend fun computeNewLoaderState(currentLoaderState: LoaderState?, request: LoaderRequest, isFilterApplied: Boolean): LoaderState? {
    return when(request) {
      LoaderRequest.RESET -> {
        null
      }
      LoaderRequest.LOAD_MORE -> {
        if (currentLoaderState?.isFinished == true) return currentLoaderState
        try {
          _isLoadingStateFlow.value = true
          var newLoaderState = currentLoaderState ?: LoaderState()
          if (!isFilterApplied) {
            val teamsResponse = currentLoaderState?.teams?.let { QDCloudResponse.Success(it) } ?: loadAllTeams()
            val teams = when (teamsResponse) {
              is QDCloudResponse.Error -> return newLoaderState.copy(latestPageResponse = teamsResponse)
              is QDCloudResponse.Success -> teamsResponse.value
            }
            newLoaderState = newLoaderState.copy(teams = teams)
            newLoaderState = loadNextProjectPage(newLoaderState)
            newLoaderState
          } else {
            val teamsResponse = loadAllTeams()
            val teams = when(teamsResponse) {
              is QDCloudResponse.Error -> return newLoaderState.copy(latestPageResponse = teamsResponse)
              is QDCloudResponse.Success -> teamsResponse.value
            }.toSet()

            val originUrl = RepositoryInfoProvider.getProjectOriginUrl(project)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
            val projects = getRelatedProjectsByOriginUrl(originUrl)
            newLoaderState.copy(hasMorePages = false, latestPageResponse = QDCloudResponse.Success(
              projects.distinctBy { it.projectId }.mapNotNull { project ->
                val date = try {
                  project.reportInfo?.lastChecked?.let { lastChecked -> dateFormat.parse(lastChecked) }
                }
                catch (_: ParseException) {
                  null
                }
                val team = teams.firstOrNull { team -> team.name == project.teamName }
                if (team == null) {
                  return@mapNotNull null // possible for public projects
                }
                ProjectData(
                  project.projectId,
                  team,
                  project.projectName,
                  project.reportInfo?.problems?.total,
                  project.reportInfo?.baselineCount,
                  project.reportInfo?.branch,
                  date,
                  project.reportInfo?.url
                )
              }
            ))
          }
        }
        finally {
          _isLoadingStateFlow.value = false
        }
      }
    }
  }

  private suspend fun loadAllTeams(): QDCloudResponse<List<TeamData>> {
    return qodanaCloudResponse {
      val api = authorized.userApi().value()

      val organizations = api.getOrganizations().value()
      coroutineScope {
        val teams = organizations.map { organizationResponse ->
          async {
            val teams = api.getTeams(organizationResponse.id, QDCloudRequestParameters.Paginated(0, Int.MAX_VALUE)).value()
            teams.items.map {
              TeamData(it.id, organizationResponse.id, it.name, it.projectCount, it.membersCount)
            }
          }
        }.awaitAll().flatten()
        sortTeamsByProjectRelevance(teams)
      }
    }
  }

  private suspend fun loadNextProjectPage(currentLoaderState: LoaderState): LoaderState {
    val teams = currentLoaderState.teams
    val projectsPage = mutableListOf<ProjectData>()
    val pageProperties = PageProperties(currentLoaderState.currentTeamIndex, currentLoaderState.currentProjectPageInTeamOffset)

    var currentTeam = teams.getOrNull(pageProperties.currentTeamIndex)
    while (currentTeam != null && projectsPage.size < pageSize) {
      when(val projectsResponse = getProjectsFromTeam(currentTeam, pageProperties)) {
        is QDCloudResponse.Error -> {
          return currentLoaderState.copy(latestPageResponse = projectsResponse)
        }
        is QDCloudResponse.Success -> {
          projectsPage += projectsResponse.value
        }
      }
      currentTeam = teams.getOrNull(pageProperties.currentTeamIndex)
    }

    return LoaderState(
      teams,
      pageProperties.currentTeamIndex,
      pageProperties.currentProjectPageInTeamOffset,
      hasMorePages = currentTeam != null,
      QDCloudResponse.Success(projectsPage)
    )
  }

  private class PageProperties(var currentTeamIndex: Int, var currentProjectPageInTeamOffset: Int)

  private suspend fun getProjectsFromTeam(team: TeamData, pageProperties: PageProperties): QDCloudResponse<List<ProjectData>> {
    return qodanaCloudResponse {
      val projectsPaginated = authorized.userApi().value()
        .getProjectsOfTeam(team.id, QDCloudRequestParameters.Paginated(pageProperties.currentProjectPageInTeamOffset, pageSize)).value()

      val nextPageOffset = projectsPaginated.nextPageOffset
      if (nextPageOffset == null) {
        pageProperties.currentTeamIndex++
        pageProperties.currentProjectPageInTeamOffset = 0
      } else {
        pageProperties.currentProjectPageInTeamOffset = nextPageOffset
      }

      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
      projectsPaginated.items.map {
        val date = try {
          it.lastChecked?.let { lastChecked -> dateFormat.parse(lastChecked) }
        }
        catch (_: ParseException) {
          null
        }
        ProjectData(it.id, team, it.name, it.problems?.total, it.baselineCount, it.branch, date, it.url)
      }
    }
  }

  private fun sortTeamsByProjectRelevance(teams: List<TeamData>): List<TeamData> {
    val organizationIdToOrganizationInitialOrder: Map<String, Int> = teams
      .map { it.organizationId }
      .distinct()
      .mapIndexed { idx, organizationId ->
        organizationId to idx
      }.toMap()

    return teams
      .map { team ->
        val comparable = TeamDataComparable(
          organizationIdToOrganizationInitialOrder[team.organizationId]!!,
          team.name?.contains(project.name, ignoreCase = true) == true,
          team.membersCount
        )
        team to comparable
      }
      .sortedWith(
        compareBy<Pair<TeamData, TeamDataComparable>> { it.second.organizationInitialOrder }
          .then(compareByDescending { it.second.isTeamNameRelevantToProject })
          .then(compareByDescending { it.second.membersCount })
      )
      .map {
        it.first
      }
  }

  private suspend fun getRelatedProjectsByOriginUrl(originUrl: String?): List<QDCloudSchema.MatchingProject> {
    originUrl ?: return emptyList()
    val plainOrigin = originUrl.removeSuffix(".git")

    return coroutineScope {
      getAllGitLinks(plainOrigin).map {
        async {
          val projectsResponse = qodanaCloudResponse {
            authorized.userApi().value()
              .getProjectByOriginUrl(it).value()
          }
          when (projectsResponse) {
            is QDCloudResponse.Error -> emptyList()
            is QDCloudResponse.Success -> {
              projectsResponse.value.matchingProjects
            }
          }
        }
      }.awaitAll().flatten().sortedBy { it.teamId }
    }
  }

  private fun getAllGitLinks(url: String): List<String> {
    val isSsh = url.startsWith("git@")
    val httpsUrl = if (!isSsh) {
      url
    } else {
      "https://${url.substringAfter("git@").replace(":", "/")}"
    }
    val sshPattern = "https://([^/]*)/(.*)".toRegex()
    val matchResult = sshPattern.matchEntire(httpsUrl) ?: return listOf(url)
    val (domain, path) = matchResult.destructured
    val sshUrl = "git@$domain:$path"
    return listOf(httpsUrl, "$httpsUrl.git", sshUrl, "$sshUrl.git", "ssh://$sshUrl", "ssh://$sshUrl.git")
  }

  private class TeamDataComparable(
    val organizationInitialOrder: Int,
    val isTeamNameRelevantToProject: Boolean,
    val membersCount: Int
  )
}