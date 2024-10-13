@file:OptIn(FlowPreview::class)

package org.jetbrains.qodana.cloud.project

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.EditorBundle
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.QodanaIntelliJYamlService
import org.jetbrains.qodana.cloud.CloudUserPrimaryData
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.StateManager
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.highlightedReportDataIfSelected
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.settings.qodanaSettings
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.SourceLinkState
import org.jetbrains.qodana.stats.StatsReportType
import org.jetbrains.qodana.ui.problemsView.QodanaProblemsViewTab
import org.jetbrains.qodana.vcs.trimRevisionString
import kotlin.time.Duration.Companion.seconds

/**
 * Service responsible for linked qodana cloud project with IDE project
 *
 * See [linkState] flow to retrieve current state of link with cloud project.
 */
@State(name = "QodanaCloudProjectLink", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE, roamingType = RoamingType.DEFAULT)])
@Service(Service.Level.PROJECT)
class QodanaCloudProjectLinkService(private val project: Project, scope: CoroutineScope) :
  PersistentStateComponent<QodanaCloudProjectLinkService.ServiceState> {
  companion object {
    fun getInstance(project: Project): QodanaCloudProjectLinkService = project.service()
  }

  @VisibleForTesting
  var refreshedReportTimeoutBeforeNotification = 30.seconds

  private val stateManager = StateManager<LinkState> { NotLinkedImpl() }
  val linkState: StateFlow<LinkState>
    get() {
      // why launch coroutines only from here: QD-6818
      stateUpdatesSubscription.start()
      return stateManager.state
    }

  private val stateUpdatesSubscription = scope.launch(QodanaDispatchers.Default, CoroutineStart.LAZY) {
    launch {
      observeLinkStateUpdates()
    }
    launch {
      observeUserStateUpdates()
    }
  }

  private suspend fun observeLinkStateUpdates() {
    linkState.collectLatest { linkState ->
      when(linkState) {
        is LinkedImpl -> {
          supervisorScope {
            launch {
              linkState.projectDataProvider.startComputeRequestsProcessing()
            }
            launch {
              linkState.projectDataProvider.refreshLoop()
            }
            launch {
              linkState.spawnNotificationOnReportIdUpdates()
            }
            launch {
              autoLoadLatestLinkedCloudReportIfNeeded(linkState)
            }
          }
        }
        else -> {}
      }
    }
  }

  private suspend fun observeUserStateUpdates() {
    QodanaCloudStateService.userStateUpdates.collectLatest { updatedUserState ->
      supervisorScope {
        launch {
          val linkState = linkState.value
          when {
            updatedUserState !is UserState.Authorized && linkState is LinkState.Linked -> {
              linkState.unlink()
              logUnlinkWhenNotAuthorizedStats()
            }
            updatedUserState is UserState.Authorized && linkState is LinkState.NotLinked -> {
              val defaultState = QodanaIntelliJYamlService.getInstance(project).cloudProjectPrimaryData
              if (defaultState != null) {
                linkWithCloudProjectAndApply(
                  project,
                  CloudProjectData(defaultState, CloudProjectProperties(null)),
                  SourceLinkState.AUTO_LINK
                )
              }
            }
            updatedUserState is UserState.Authorized && linkState is LinkState.Linked && linkState.authorized !== updatedUserState -> {
              linkState.unlink()
              logUnlinkWhenNotAuthorizedStats()
            }
          }
        }
      }
    }
  }

  private suspend fun autoLoadLatestLinkedCloudReportIfNeeded(linked: LinkState.Linked) {
    project.qodanaSettings().loadMatchingCloudReportAutomatically.collectLatest { isAutoLoad ->
      if (!isAutoLoad) return@collectLatest

      linked.projectDataProvider.fetchedReportProperty.propertyState
        .filter { !it.isRefreshing }
        .mapNotNull { it.lastLoadedValue }
        .filter { it.isNotificationNeeded }
        .mapNotNull { it.response.asSuccess()?.reportId }
        .distinctUntilChanged()
        .collectLatest { reportId ->
          val highlightService = QodanaHighlightedReportService.getInstance(project)
          val highlightState = highlightService.highlightedReportState.value

          if (highlightState.highlightedReportDataIfSelected?.sourceReportDescriptor is LinkedCloudReportDescriptor) {
            val newReportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptorWithId(reportId)
            highlightService.highlightReport(newReportDescriptor, withFocus = false)
            logHighlightFromNotificationStats(project, SourceHighlight.CLOUD_AUTO_LOAD_LATEST)
          }
        }
    }
  }

  private fun logUnlinkWhenNotAuthorizedStats() {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_LINK.log(
      project,
      false,
      SourceLinkState.UNAUTHORIZED
    )
  }

  @Deprecated("use only for statistics, doesn't launch any state-observing coroutines, QD-6818")
  fun getIsLinkedForStats(): Boolean {
    return when(stateManager.state.value) {
      is LinkState.Linked -> true
      is LinkState.NotLinked -> false
    }
  }

  private inner class LinkedImpl(
    override val authorized: UserState.Authorized,
    cloudProjectPrimaryData: CloudProjectPrimaryData,
    cloudProjectProperties: CloudProjectProperties?,
    private val initialReportId: String?,
  ) : LinkState.Linked {
    override val projectDataProvider = QodanaCloudProjectDataProvider(
      project,
      authorized,
      cloudProjectPrimaryData,
      cloudProjectProperties,
      initialReportId,
      refreshedReportTimeoutBeforeNotification
    )

    override val cloudReportDescriptorBuilder = CloudReportDescriptorBuilder(this, project)

    override fun unlink(): LinkState.NotLinked? = stateManager.changeState(this, NotLinkedImpl())

    suspend fun spawnNotificationOnReportIdUpdates() {
      coroutineScope {
        launch {
          val newReportAppearedNotificationProvider = NewReportAppearedNotificationProvider(project)
          if (!newReportAppearedNotificationProvider.isEnabled) return@launch
          val notificationSubscriptionJob = coroutineContext.job

          var isFirstReport: Boolean = (initialReportId == null)
          projectDataProvider.latestReportForNotificationFlow.collectLatest { newReport ->
            if (newReport == null) return@collectLatest
            if (QodanaProblemsViewTab.isVisible(project)) return@collectLatest

            val notification = newReportAppearedNotificationProvider.getNotification(this@LinkedImpl, newReport, isFirstReport) {
              notificationSubscriptionJob.cancel()
            }
            withContext(QodanaDispatchers.Ui) {
              notification.notify(project)
            }
            isFirstReport = false
            try {
              awaitCancellation()
            }
            finally {
              notification.expire()
            }
          }
        }
      }
    }
  }

  private inner class NotLinkedImpl : LinkState.NotLinked {
    override fun linkWithQodanaCloudProject(authorized: UserState.Authorized, cloudProjectData: CloudProjectData): LinkState.Linked? {
      val linked = LinkedImpl(authorized, cloudProjectData.primaryData, cloudProjectData.properties, null)
      return stateManager.changeState(this, linked)
    }
  }

  override fun getState(): ServiceState {
    // value from state manager to not trigger coroutines QD-6818
    val currentLinkState = stateManager.state.value
    if (currentLinkState !is LinkedImpl) return ServiceState()

    val projectDataProvider = currentLinkState.projectDataProvider
    val lastLoadedProjectProperties = projectDataProvider.projectProperties.value.lastLoadedValue
    val projectPrimaryData = projectDataProvider.projectPrimaryData

    return ServiceState().apply {
      userId = currentLinkState.authorized.userDataProvider.cloudUserPrimaryData.id
      linkedProjectId = projectPrimaryData.id
      organizationId = projectPrimaryData.cloudOrganization.id
      linkedProjectName = lastLoadedProjectProperties?.asSuccess()?.name
      lastReportId = projectDataProvider.latestReportId
    }
  }

  override fun loadState(state: ServiceState) {
    val linkStateObtainedFromPersistent = getLinkStateFromPersisted(state)
    stateManager.changeState(linkState.value, linkStateObtainedFromPersistent)
  }

  private fun getLinkStateFromPersisted(persistentState: ServiceState): LinkState {
    val notLinked = NotLinkedImpl()
    val projectId = persistentState.linkedProjectId ?: return notLinked
    val organizationId = persistentState.organizationId ?: return notLinked
    val lastReportId = persistentState.lastReportId
    val userId = persistentState.userId

    val authorized = QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized ?: return notLinked
    if (userId != null) {
      val userPrimaryDataFromLinkedPersisted = CloudUserPrimaryData(userId)
      if (userPrimaryDataFromLinkedPersisted != authorized.userDataProvider.cloudUserPrimaryData) {
        return notLinked
      }
    }

    return LinkedImpl(
      authorized,
      CloudProjectPrimaryData(projectId, CloudOrganizationPrimaryData(organizationId)),
      CloudProjectProperties(persistentState.linkedProjectName),
      lastReportId
    )
  }


  class ServiceState : BaseState() {
    var userId: String? by string()

    var linkedProjectId: String? by string()

    var organizationId: String? by string()

    var linkedProjectName: String? by string()

    var lastReportId: String? by string()
  }
}

private class NewReportAppearedNotificationProvider(private val project: Project) {
  val isEnabled: Boolean
    get() = constructNotificationBase().canShowFor(project)

  private fun constructNotificationBase(): Notification {
    return QodanaNotifications.General.notification(
      null,
      "",
      NotificationType.INFORMATION,
      withQodanaIcon = true
    ).apply {
      configureDoNotAskOption("qodana.cloud.show.new.report.notification", QodanaBundle.message("qodana.cloud.new.report.appeared.text"))
    }
  }

  fun getNotification(
    linked: LinkState.Linked,
    newReport: QodanaCloudProjectDataProvider.CloudReport,
    isFirst: Boolean,
    doNotShowAgainAction: () -> Unit
  ): Notification {
    val notificationText = when {
      newReport.revision != null -> {
        QodanaBundle.message("qodana.cloud.report.from.commit.available", newReport.revision.trimRevisionString())
      }
      isFirst -> {
        QodanaBundle.message("qodana.cloud.available.report.text")
      }
      else -> {
        QodanaBundle.message("qodana.cloud.new.report.appeared.text")
      }
    }
    val loadReportAction = suspend {
      val reportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptorWithId(newReport.reportId)
      QodanaHighlightedReportService.getInstance(project).highlightReport(reportDescriptor)
      logHighlightFromNotificationStats(project, SourceHighlight.CLOUD_HIGHLIGHT_NEW_REPORT_APPEARED_NOTIFICATION)
    }

    val notification = constructNotificationBase()
    return notification.apply {
      setContent(notificationText)

      addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("qodana.cloud.new.report.action.load")) {
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          loadReportAction.invoke()
        }
      })
      val qodanaSettings = project.qodanaSettings()
      if (!qodanaSettings.loadMatchingCloudReportAutomatically.value) {
        addAction(NotificationAction.createSimpleExpiring(QodanaBundle.message("qodana.cloud.new.report.auto.load")) {
          project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
            loadReportAction.invoke()
            qodanaSettings.setLoadMatchingCloudReportAutomatically(true)
          }
        })
      }
      addAction(NotificationAction.createSimpleExpiring(EditorBundle.message("notification.dont.show.again.message")) {
        setDoNotAskFor(project)
        doNotShowAgainAction.invoke()
      })
    }
  }
}

private fun logHighlightFromNotificationStats(project: Project, source: SourceHighlight) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    true,
    StatsReportType.CLOUD,
    source
  )
}