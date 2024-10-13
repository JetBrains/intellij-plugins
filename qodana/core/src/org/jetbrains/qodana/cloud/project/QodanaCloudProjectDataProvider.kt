package org.jetbrains.qodana.cloud.project

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import org.jetbrains.qodana.cloud.RefreshableProperty
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudRequestParameters
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.vcs.VcsRevisionService
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private val PROJECT_INFO_REFRESH_PERIOD = 15.minutes
private val REPORT_ID_REFRESH_PERIOD = 5.minutes
private val HEURISTIC_PERIOD_BETWEEN_COMMIT_AND_ANALYSIS = 2.days

private val LOG = logger<QodanaCloudProjectDataProvider>()

class QodanaCloudProjectDataProvider(
  private val project: Project,
  private val authorized: UserState.Authorized,
  val projectPrimaryData: CloudProjectPrimaryData,
  initialProjectProperties: CloudProjectProperties?,
  private val initialReportId: String?,
  private val newReportNotificationTimeout: Duration,
) {
  val projectId: String
    get() = projectPrimaryData.id

  private val projectPropertiesRefreshable = RefreshableProperty<QDCloudResponse<CloudProjectProperties>>(
    PROJECT_INFO_REFRESH_PERIOD,
    initialProjectProperties?.let { QDCloudResponse.Success(it) }
  ) {
    qodanaCloudResponse {
      val propertiesResponse = authorized.userApi().value()
        .getProjectProperties(projectId).value()
      @Suppress("HardCodedStringLiteral")
      CloudProjectProperties(propertiesResponse.name)
    }
  }

  val projectProperties: StateFlow<RefreshableProperty.PropertyState<QDCloudResponse<CloudProjectProperties>>> =
    projectPropertiesRefreshable.propertyState

  @Suppress("RemoveExplicitTypeArguments")
  val fetchedReportProperty = RefreshableProperty<FetchedReportResponse>(
    REPORT_ID_REFRESH_PERIOD,
    initialReportId?.let { FetchedReportResponse(QDCloudResponse.Success(CloudReport(initialReportId, revision = null)), isNotificationNeeded = false) },
    ::fetchReport
  )

  data class FetchedReportResponse(val response: QDCloudResponse<CloudReport?>, val isNotificationNeeded: Boolean)

  data class CloudReport(val reportId: String, @NlsSafe val revision: String?)

  val latestReportId: String?
    get() {
      return fetchedReportProperty.propertyState.value.lastLoadedValue?.response?.asSuccess()?.reportId
    }

  private val _latestReportForNotificationFlow = MutableSharedFlow<CloudReport?>(replay = 1)
  val latestReportForNotificationFlow: SharedFlow<CloudReport?> = _latestReportForNotificationFlow.asSharedFlow()

  private data class LatestReport(val report: CloudReport, val isNotificationNeeded: Boolean)

  @OptIn(FlowPreview::class)
  private suspend fun processLatestReportIdForNotificationFlow() {
    var lastSuccessReportId: String? = initialReportId
    fetchedReportProperty.propertyState
      .map { newReportIdState ->
        val fetchedReportId = newReportIdState.lastLoadedValue ?: return@map null
        val report = fetchedReportId.response.asSuccess() ?: return@map null

        LatestReport(report, fetchedReportId.isNotificationNeeded)
      }
      .debounce { latestRefreshedReportId ->
        if (latestRefreshedReportId?.isNotificationNeeded == true) newReportNotificationTimeout else Duration.ZERO
      }
      .distinctUntilChanged()
      .collectLatest { latestReport ->
        if (latestReport == null) {
          _latestReportForNotificationFlow.emit(null)
          return@collectLatest
        }

        val reportId = latestReport.report.reportId
        val isNotificationNeeded = latestReport.isNotificationNeeded
        val isNewReport = (reportId != lastSuccessReportId)
        lastSuccessReportId = reportId
        when {
          !isNotificationNeeded -> {
            _latestReportForNotificationFlow.emit(null)
          }
          isNewReport -> {
            _latestReportForNotificationFlow.emit(latestReport.report)
          }
        }
      }
  }

  private suspend fun fetchReport(oldReportResponse: FetchedReportResponse?): FetchedReportResponse {
    val reportBestMatchingCurrentRevision = getLatestReportBestMatchingCurrentRevision()
    val cloudReport: QDCloudResponse<CloudReport?> = when (reportBestMatchingCurrentRevision) {
      is QDCloudResponse.Error -> {
        reportBestMatchingCurrentRevision
      }
      is QDCloudResponse.Success -> {
        reportBestMatchingCurrentRevision.value?.let { QDCloudResponse.Success(it) } ?: getLatestReport()
      }
    }
    val isNewReport = when (cloudReport) {
      is QDCloudResponse.Success -> cloudReport.value?.reportId != oldReportResponse?.response?.asSuccess()?.reportId
      is QDCloudResponse.Error -> false
    }
    return FetchedReportResponse(cloudReport, isNotificationNeeded = (oldReportResponse?.isNotificationNeeded == true) || isNewReport)
  }

  private suspend fun getLatestReportBestMatchingCurrentRevision(): QDCloudResponse<CloudReport?> {
    val revisionPagedLoader = VcsRevisionService.getInstance(project).revisionPagedLoaderFlow.first()

    var start = Instant.now()
    return revisionPagedLoader
      .startRevisionPagesCalculation()
      .mapNotNull { page ->
        if (page.revisions.isEmpty()) return@mapNotNull null

        val startPeriod = start.plusMillis(HEURISTIC_PERIOD_BETWEEN_COMMIT_AND_ANALYSIS.inWholeMilliseconds)
        val end = page.revisions.last().date
        start = end

        val reportsWithRevisionsForPeriodResponse = qodanaCloudResponse {
          val api = authorized.userApi().value()

          LOG.debug { "Checking if any report on cloud is present before $startPeriod" }
          val isAnyReportPresent = api.getReportsWithRevisionsForPeriod(
            projectId,
            from = null,
            to = startPeriod,
            QDCloudRequestParameters.Paginated(offset = 0, limit = 1)
          ).value().items.isNotEmpty()
          if (!isAnyReportPresent) return@qodanaCloudResponse null

          api.getReportsWithRevisionsForPeriod(
            projectId,
            from = end,
            to = startPeriod,
            QDCloudRequestParameters.Paginated(offset = 0, limit = Int.MAX_VALUE)
          ).value().items
        }
        LOG.debug { "Reports on cloud for $end, $startPeriod : $reportsWithRevisionsForPeriodResponse" }

        when(reportsWithRevisionsForPeriodResponse) {
          is QDCloudResponse.Error -> {
            return@mapNotNull reportsWithRevisionsForPeriodResponse
          }
          is QDCloudResponse.Success -> {
            val reportsWithRevisionsForPeriod: List<QDCloudSchema.ReportWithRevision>? = reportsWithRevisionsForPeriodResponse.value
            if (reportsWithRevisionsForPeriod == null) return@mapNotNull QDCloudResponse.Success(null)

            val revisionIdToCloudReport = reportsWithRevisionsForPeriod.filter { it.commit != null }.asReversed().associateBy { it.commit }
            val matchingReport = page.revisions.firstNotNullOfOrNull { revision -> revisionIdToCloudReport[revision.id] } ?: return@mapNotNull null
            return@mapNotNull QDCloudResponse.Success(CloudReport(reportId = matchingReport.reportId, revision = matchingReport.commit))
          }
        }
      }
      .flowOn(QodanaDispatchers.Default)
      .firstOrNull() ?: QDCloudResponse.Success(null)
  }

  private suspend fun getLatestReport(): QDCloudResponse<CloudReport?> {
    return qodanaCloudResponse {
      val timelineRequestResponse = authorized.userApi().value()
        .getReportsTimeline(
          projectId,
          listOf(
            QDCloudRequestParameters.ReportState.UPLOADED,
            QDCloudRequestParameters.ReportState.PROCESSED,
            QDCloudRequestParameters.ReportState.PINNED
          ),
          QDCloudRequestParameters.Paginated(offset = 0, limit = 1)
        ).value()
      val reportResponse = timelineRequestResponse.items.firstOrNull()
      if (reportResponse == null) {
        thisLogger().warn("No reports found")
      }
      reportResponse?.reportId?.let { CloudReport(it, revision = null) }
    }
  }

  suspend fun refreshLatestReportIdWithoutNotification(): QDCloudResponse<String?>? {
    val loadedReportResponse = fetchedReportProperty.refreshManually {
      it?.copy(isNotificationNeeded = false)
    }.lastLoadedValue?.response ?: return null

    return qodanaCloudResponse {
      loadedReportResponse.value()?.reportId
    }
  }

  suspend fun setLatestNoNotificationReportId(reportId: String) {
    val fetchedReport = FetchedReportResponse(QDCloudResponse.Success(CloudReport(reportId, revision = null)), isNotificationNeeded = false)
    fetchedReportProperty.setValue(RefreshableProperty.PropertyState(fetchedReport, isRefreshing = false))
  }

  @TestOnly
  suspend fun refreshLatestReportIdWithNotification() {
    fetchedReportProperty.refreshManually()
  }

  suspend fun startComputeRequestsProcessing() {
    val properties = listOf(projectPropertiesRefreshable, fetchedReportProperty)
    coroutineScope {
      properties.forEach {
        launch {
          it.startRequestsProcessing()
        }
      }
      launch {
        processLatestReportIdForNotificationFlow()
      }
    }
  }

  suspend fun refreshLoop() {
    coroutineScope {
      launch {
        VcsRevisionService.getInstance(project).revisionPagedLoaderFlow.collectLatest {
          fetchedReportProperty.refreshLoop()
        }
      }
      launch {
        projectPropertiesRefreshable.refreshLoop()
      }
    }
  }
}