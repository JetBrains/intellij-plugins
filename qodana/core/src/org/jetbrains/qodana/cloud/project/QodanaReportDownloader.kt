package org.jetbrains.qodana.cloud.project

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.getProjectDataPath
import com.intellij.platform.util.progress.ProgressReporter0
import com.intellij.platform.util.progress.asContextElement
import com.intellij.platform.util.progress.progressReporter
import com.intellij.platform.util.progress.progressStep
import com.intellij.util.io.copy
import com.intellij.util.io.createDirectories
import com.intellij.util.io.delete
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.XCollection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.getErrorNotification
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.report.*
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.pathString

internal class DownloadedReportInfo {
  @get:Attribute
  var reportId: String? = null

  @get:Attribute
  var path: String? = null

  @get:Attribute
  var reportArtifactFiles: String? = null
}

private sealed interface DownloaderRequest {
  data class DownloadReport(
    val authorized: UserState.Authorized,
    val reportId: String,
    val projectId: String,
    val doDownload: Boolean,
    val progressReporter: ProgressReporter0? = null
  ) : DownloaderRequest {
    val loadedReportDeferred = CompletableDeferred<Deferred<LoadedReport.Sarif?>>()
  }

  data class InitReports(val reportsMap: Map<String, DownloadedReportInfo>) : DownloaderRequest

  object Clear : DownloaderRequest

  data class AddPublishedReport(
    val reportPath: Path,
    val projectId: String,
    val reportId: String
  ) : DownloaderRequest {
    val completed = CompletableDeferred<Unit>()
  }
}

internal class ReportDownloaderState : BaseState() {
  @get:XCollection(style = XCollection.Style.v2)
  var loadedReportsPaths: MutableMap<String, DownloadedReportInfo> by property(HashMap()) { it.isEmpty() }
}

@State(name = "QodanaReportDownloader", storages = [Storage(value = StoragePathMacros.CACHE_FILE)])
@Service(Service.Level.PROJECT)
internal class QodanaReportDownloader(private val project: Project, private val scope: CoroutineScope): PersistentStateComponent<ReportDownloaderState> {
  companion object {
    fun getInstance(project: Project): QodanaReportDownloader = project.service()
  }

  private val downloadReportRequestsChannel = Channel<DownloaderRequest>()
  private val myInitReportsRequestChannel = Channel<DownloaderRequest.InitReports>()

  private val loadedReportsPathsUpdatesStateFlow: StateFlow<Map<String, DownloadedReportInfo>> = getLoadedReportsPathsUpdatesStateFlow()

  override fun getState(): ReportDownloaderState {
    return ReportDownloaderState().apply { this.loadedReportsPaths = loadedReportsPathsUpdatesStateFlow.value.toMutableMap() }
  }

  override fun loadState(state: ReportDownloaderState) {
    scope.launch(QodanaDispatchers.Default) {
      myInitReportsRequestChannel.send(DownloaderRequest.InitReports(state.loadedReportsPaths))
    }
  }

  override fun noStateLoaded() {
    scope.launch(QodanaDispatchers.Default) {
      myInitReportsRequestChannel.send(DownloaderRequest.InitReports(emptyMap()))
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun getLoadedReportsPathsUpdatesStateFlow(): StateFlow<Map<String, DownloadedReportInfo>> {
    var currentReportsMap = emptyMap<String, DownloadedReportInfo>()
    return myInitReportsRequestChannel.receiveAsFlow()
      .transformLatest { setReportsRequest ->
        emit(setReportsRequest)
        emitAll(downloadReportRequestsChannel.receiveAsFlow())
      }
      .onEach { request ->
        supervisorScope {
          launch {
            thisLogger().info("Processing request $request")
            val newReportsMap = when (request) {
              is DownloaderRequest.DownloadReport -> {
                processDownloadReportWithArtifacts(currentReportsMap, request)
              }
              is DownloaderRequest.AddPublishedReport -> {
                processAddPublishedReport(currentReportsMap, request)
              }
              is DownloaderRequest.InitReports -> {
                request.reportsMap.toMap()
              }
              is DownloaderRequest.Clear -> {
                emptyMap()
              }
            }
            clearOutdatedReports(oldReportsMap = currentReportsMap, newReportsMap = newReportsMap)
            currentReportsMap = newReportsMap
          }
        }
      }
      .map {
        currentReportsMap
      }
      .flowOn(QodanaDispatchers.Default)
      .stateIn(scope, SharingStarted.Eagerly, emptyMap())
  }

  suspend fun getReport(
    authorized: UserState.Authorized,
    reportId: String,
    projectId: String,
    doDownload: Boolean,
  ): LoadedReport.Sarif? {
    val request = DownloaderRequest.DownloadReport(authorized, reportId, projectId, doDownload, coroutineContext.progressReporter)
    val loadedReport = try {
      downloadReportRequestsChannel.send(request)
      request.loadedReportDeferred.await()
    }
    catch (ce : CancellationException) {
      request.loadedReportDeferred.cancel()
      throw ce
    }

    return try {
      loadedReport.await()
    } catch (ce: CancellationException) {
      loadedReport.cancel()
      throw ce
    }
  }

  suspend fun addPublishedReport(reportPath: Path, projectId: String, reportId: String) {
    val request = DownloaderRequest.AddPublishedReport(reportPath, projectId, reportId)
    downloadReportRequestsChannel.send(request)
    request.completed.join()
  }

  suspend fun clearAllReports() {
    downloadReportRequestsChannel.send(DownloaderRequest.Clear)
  }

  private suspend fun processAddPublishedReport(
    oldReportsMap: Map<String, DownloadedReportInfo>,
    request: DownloaderRequest.AddPublishedReport
  ): Map<String, DownloadedReportInfo> {
    val requestedProjectId = request.projectId
    val requestedReportId = request.reportId
    if (oldReportsMap[requestedProjectId]?.reportId == requestedReportId) {
      return oldReportsMap
    }

    val reportPath = try {
      val projectDataDirectory = getProjectDownloaderDataDirectory(project)
      val copiedPath = runInterruptible(QodanaDispatchers.IO) {
        request.reportPath.copy(projectDataDirectory.resolve(getDownloadedSarifReportName(requestedReportId)))
      }
      copiedPath
    } catch (e: IOException) {
      request.reportPath
    }
    val newReportInfo = DownloadedReportInfo().apply {
      this.reportId = requestedReportId
      this.path = reportPath.toString()
      this.reportArtifactFiles = getProjectDownloaderDataDirectory(project, requestedReportId).toString()
    }
    request.completed.complete(Unit)
    return oldReportsMap.toMutableMap().apply {
      put(requestedProjectId, newReportInfo)
    }
  }

  private suspend fun processDownloadReportWithArtifacts(
    oldReportsMap: Map<String, DownloadedReportInfo>,
    request: DownloaderRequest.DownloadReport
  ): Map<String, DownloadedReportInfo> {
    val reportsMapToUpdate = oldReportsMap.toMutableMap()
    val task = request.loadedReportDeferred
    val progressReporter = request.progressReporter
    try {
      return withContext(progressReporter?.asContextElement() ?: EmptyCoroutineContext) {
        val loadedReport = async {
          val projectName = getProjectName(request.authorized, request.projectId)
          if (projectName == null) {
            return@async null
          }
          val report = progressStep(0.5, QodanaBundle.message("progress.title.qodana.loading.report.data.from.cloud")) {
            processDownloadReport(request, reportsMapToUpdate)
          }
          if (report == null) {
            return@async null
          }
          val artifacts = progressStep(1.0, QodanaBundle.message("progress.title.qodana.loading.coverage.data.from.cloud")) {
            processDownloadReportArtifacts(request, reportsMapToUpdate.toMap())
          }
          LoadedReport.Sarif(report, AggregatedReportMetadata(artifacts), projectName)
        }
        task.complete(loadedReport)
        task.await().await()
        reportsMapToUpdate
      }
    }
    catch (e: Exception) {
      withContext(NonCancellable) {
        clearNotUsedFilesInDownloaderDir(oldReportsMap.values)
      }
      task.completeExceptionally(e)
      throw e
    }
  }

  private suspend fun getProjectName(authorized: UserState.Authorized, projectId: String): String? {
    val projectNameResponse = qodanaCloudResponse {
      authorized.userApi().value()
        .getProjectProperties(projectId).value().name
    }
    return when(projectNameResponse) {
      is QDCloudResponse.Success -> {
        projectNameResponse.value
      }
      is QDCloudResponse.Error -> {
        projectNameResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.project.name.failed.load")).notify(project)
        null
      }
    }
  }

  private suspend fun processDownloadReport(
    request: DownloaderRequest.DownloadReport,
    reportsMapToUpdate: MutableMap<String, DownloadedReportInfo>
  ): ValidatedSarif? {
    val projectId = request.projectId
    val reportId = request.reportId

    if (projectId in reportsMapToUpdate) {
      val downloadedReportData = reportsMapToUpdate[projectId]
      val savedReportPath = downloadedReportData?.path

      if (savedReportPath != null && downloadedReportData.reportId == reportId) {
        val report = runInterruptible(QodanaDispatchers.IO) {
          when (val readReportResult = ReportReader.readReport(Paths.get(savedReportPath))) {
            is ReportResult.Fail -> {
              thisLogger().info("Report is no more present on disk. Trying to download it.")
              null
            }
            is ReportResult.Success -> readReportResult.loadedSarifReport
          }
        }
        if (report != null) {
          thisLogger().info("Loaded report from '$savedReportPath'")
          return report
        }
      }
    }
    reportsMapToUpdate.remove(projectId)
    if (!request.doDownload) {
      return null
    }

    val reportPath = loadFromCloud(
      getDownloadedSarifReportName(reportId),
      "qodana.sarif.json",
      getProjectDownloaderDataDirectory(project),
      urlProvider = { fetchSarifReportUrl(request.authorized, reportId) }
    )
    if (reportPath == null) {
      return null
    }
    thisLogger().info("Saved report for project $projectId with id $reportId to '$reportPath'")

    val validatedSarif = runInterruptible(QodanaDispatchers.IO) {
      when (val readReportResult = ReportReader.readReport(reportPath)) {
        is ReportResult.Fail -> {
          readReportResult.error.spawnNotification(project) {
            getReportFileErrorNotificationContent(reportPath, it)
          }
          return@runInterruptible null
        }
        is ReportResult.Success -> readReportResult.loadedSarifReport
      }
    } ?: return null

    reportsMapToUpdate[projectId] = DownloadedReportInfo().apply {
      this.reportId = reportId
      this.path = reportPath.toString()
      this.reportArtifactFiles = getProjectDownloaderDataDirectory(project, reportId).toString()
    }
    return validatedSarif
  }

  private suspend fun processDownloadReportArtifacts(
    request: DownloaderRequest.DownloadReport,
    reportsMap: Map<String, DownloadedReportInfo>
  ): Map<String, ReportMetadata> {
    val requestedProjectId = request.projectId
    val requestedReportId = request.reportId
    return coroutineScope {
      ReportMetadataArtifactProvider.EP_NAME.extensionList.map { provider ->
        async {
          val downloadedReportData = reportsMap[requestedProjectId] ?: return@async null
          val savedReportPath = downloadedReportData.reportArtifactFiles ?: return@async null

          if (downloadedReportData.reportId != requestedReportId) return@async null

          val report = let {
            val path = Path.of(savedReportPath).resolve(provider.presentableFileName)
            if (!path.exists()) return@let null
            provider.readReport(path)
          }
          if (report != null) {
            thisLogger().info("Loaded report from '$savedReportPath' for '${provider.name}'")
            return@async report
          }
          if (!request.doDownload) {
            return@async null
          }
          val reportPath = loadFromCloud(
            provider.presentableFileName, // we should download with the name equal to presentable, that does not contain path
            provider.presentableFileName,
            Path.of(savedReportPath),
            urlProvider = { fetchArtifactUrl(request.authorized, requestedReportId, provider.fileName) }
          )
          if (reportPath == null) {
            return@async null
          }

          return@async provider.readReport(reportPath)
        }
      }.awaitAll().filterNotNull().associateBy { it.id }
    }
  }

  private suspend fun loadFromCloud(
    fileName: String,
    presentableFileName: String,
    path: Path,
    urlProvider: suspend () -> String?
  ): Path? {
    val reportUrl = urlProvider() ?: return null
    withContext(QodanaDispatchers.IO) {
      removeReportByPath(path.resolve(fileName).toString())
    }
    return QodanaArtifactsDownloader.getInstance().download(
      url = reportUrl,
      filename = fileName,
      presentableUrl = "qodana.cloud",
      presentableFilename = presentableFileName,
      pathToDownload = path
    )?.toPath()
  }

  private suspend fun fetchSarifReportUrl(authorized: UserState.Authorized, reportId: String): String? {
    val sarifFilesResponse = qodanaCloudResponse {
      authorized.userApi().value()
        .getReportFiles(reportId, listOf("qodana.sarif.json")).value()
    }
    return when (sarifFilesResponse) {
      is QDCloudResponse.Success -> {
        val sarifUrl = sarifFilesResponse.value.files.firstOrNull()?.url
        if (sarifUrl == null) {
          thisLogger().warn("No SARIF found in cloud")
          getCloudReportNotFoundNotification().notify(project)
        }
        sarifUrl
      }
      is QDCloudResponse.Error -> {
        thisLogger().warn("Failed loading Qodana Cloud report", sarifFilesResponse.exception)
        sarifFilesResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.report.failed.load")).notify(project)
        null
      }
    }
  }

  private suspend fun fetchArtifactUrl(
    authorized: UserState.Authorized,
    reportId: String,
    fileName: String
  ): String? {
    val artifactResponse = qodanaCloudResponse {
      authorized.userApi().value()
        .getReportFiles(reportId, listOf(fileName)).value()
    }
    return when (artifactResponse) {
      is QDCloudResponse.Success -> {
        val artifactUrl = artifactResponse.value.files.firstOrNull()?.url
        if (artifactUrl == null) {
          thisLogger().warn("No '$fileName' Qodana Cloud report's artifact found in cloud")
        }
        artifactUrl
      }
      is QDCloudResponse.Error -> {
        thisLogger().warn("Failed loading Qodana Cloud report's artifact '$fileName'", artifactResponse.exception)
        artifactResponse.getErrorNotification(QodanaBundle.message("notification.title.cloud.artifact.failed.load", fileName)).notify(project)
        null
      }
    }
  }

  private suspend fun clearOutdatedReports(
    oldReportsMap: Map<String, DownloadedReportInfo>,
    newReportsMap: Map<String, DownloadedReportInfo>
  ) {
    for ((oldProjectId, oldReportInfo) in oldReportsMap) {
      val newReport = newReportsMap[oldProjectId]
      val oldProjectIsNoLongerPresent = newReport == null

      if (oldProjectIsNoLongerPresent) {
        val oldReportPath = oldReportInfo.path
        val oldReportArtifactsPath = oldReportInfo.reportArtifactFiles
        removeReportByPath(oldReportPath)
        removeReportByPath(oldReportArtifactsPath)
      }
    }

    for ((projectId, newReportInfo) in newReportsMap) {
      val oldReportInfo = oldReportsMap[projectId] ?: continue

      val newReportId = newReportInfo.reportId
      val newReportPath = newReportInfo.path
      val newReportArtifactsPath = newReportInfo.reportArtifactFiles

      val oldReportId = oldReportInfo.reportId
      val oldReportPath = oldReportInfo.path
      val oldReportArtifactsPath = oldReportInfo.reportArtifactFiles

      if (newReportId != oldReportId) {
        removeReportByPath(oldReportPath)
        removeReportByPath(oldReportArtifactsPath)
      }
      if (newReportPath != oldReportPath) {
        removeReportByPath(oldReportPath)
      }
      if (newReportArtifactsPath != oldReportArtifactsPath) {
        removeReportByPath(oldReportArtifactsPath)
      }
    }
    clearNotUsedFilesInDownloaderDir(newReportsMap.values)
  }

  @OptIn(ExperimentalPathApi::class)
  private suspend fun clearNotUsedFilesInDownloaderDir(currentReports: Collection<DownloadedReportInfo>) {
    val reportsFiles = currentReports.flatMap { listOfNotNull(it.path, it.reportArtifactFiles) }.toSet()
    val downloaderDir = getProjectDownloaderDataDirectory(project)
    runInterruptible(QodanaDispatchers.IO) {
      val children = downloaderDir.toFile().listFiles() ?: return@runInterruptible
      children.asSequence()
        .map { it.toPath() }
        .filter { path ->
          path.pathString !in reportsFiles
        }
        .forEach { path ->
          try {
            path.deleteRecursively()
          }
          catch (_ : IOException) {
          }
        }
    }
  }

  private suspend fun removeReportByPath(path: String?) {
    if (path == null) return
    try {
      val nioPath = Paths.get(path)
      runInterruptible(QodanaDispatchers.IO) {
        if (!nioPath.exists()) return@runInterruptible
        thisLogger().info("Deleting report at $path")
        Paths.get(path).delete()
      }
    }
    catch (e: IOException) {
      thisLogger().info("Can't delete report from disk", e)
    }
  }
}

private fun getCloudReportNotFoundNotification(): Notification {
  return QodanaNotifications.General.notification(
    QodanaBundle.message("notification.title.cloud.report.failed.load"),
    LinkedCloudReportDescriptor.FailMessagesProvider.getCloudReportNotFoundMessage(),
    NotificationType.WARNING
  )
}

private suspend fun getProjectDownloaderDataDirectory(project: Project): Path {
  val projectDataDirectory = project.getProjectDataPath("QodanaDownloadedReports")
  runInterruptible(QodanaDispatchers.IO) {
    projectDataDirectory.createDirectories()
  }
  return projectDataDirectory
}

private suspend fun getProjectDownloaderDataDirectory(project: Project, reportId: String): Path {
  val projectDataDirectory = getProjectDownloaderDataDirectory(project).resolve(reportId)
  runInterruptible(QodanaDispatchers.IO) {
    projectDataDirectory.createDirectories()
  }
  return projectDataDirectory
}

private fun getDownloadedSarifReportName(reportId: String): String = "qodana_cloud_$reportId.sarif.json"