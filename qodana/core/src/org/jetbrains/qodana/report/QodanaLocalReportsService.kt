package org.jetbrains.qodana.report

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import org.jetbrains.qodana.run.LocalRunNotPublishedReportDescriptor
import org.jetbrains.qodana.run.LocalRunPublishedReportDescriptor
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

/** Container of "local" reports: manually loaded from local files or from Open in IDE */
@State(name = "QodanaReportsService", storages = [Storage(value = StoragePathMacros.WORKSPACE_FILE, roamingType = RoamingType.DISABLED)])
@Service(Service.Level.PROJECT)
class QodanaLocalReportsService(private val project: Project) : PersistentStateComponent<QodanaLocalReportsService.State> {
  companion object {
    fun getInstance(project: Project): QodanaLocalReportsService = project.service()
  }

  private val reports: MutableSet<LocalReportDescriptor> = ConcurrentHashMap<LocalReportDescriptor, Unit>().keySet(Unit)

  fun clear() {
    val clearedReports = reports.toSet()
    reports.removeAll(clearedReports)
    clearedReports.forEach { it.markAsUnavailable() }
  }

  fun addReport(reportDescriptor: LocalReportDescriptor) {
    reports.add(reportDescriptor)
  }

  fun getReports(): Set<LocalReportDescriptor> {
    val currentReports = reports.toSet()

    val (availableReports, unavailableReports) = currentReports.partition { it.checkAvailability() }
    reports.removeAll(unavailableReports.toSet())

    return availableReports.toSet()
  }

  override fun getState(): State {
    return State(reports.mapNotNull(::reportStateFromDescriptor))
  }

  override fun loadState(state: State) {
    reports.clear()
    reports.addAll(state.descriptions.mapNotNull(::reportDescriptorFromState))
  }

  private fun reportStateFromDescriptor(descriptor: LocalReportDescriptor): ReportDescriptorState? {
    return when(descriptor) {
      is FileReportDescriptor -> {
        reportStateFromFileReportDescriptor(descriptor)
      }
      is LocalRunNotPublishedReportDescriptor -> {
        val state = reportStateFromFileReportDescriptor(descriptor.fileReportDescriptor)
        state.apply {
          isLocalRun = true
        }
      }
      is LocalRunPublishedReportDescriptor -> {
        val state = reportStateFromFileReportDescriptor(descriptor.fileReportDescriptor)
        state.apply {
          isLocalRun = true
          publishedReportLink = descriptor.publishedReportLink
        }
      }
      else -> null
    }
  }

  private fun reportStateFromFileReportDescriptor(descriptor: FileReportDescriptor): ReportDescriptorState {
    return ReportDescriptorState().apply {
      reportGuid = descriptor.reportGuid
      reportId = descriptor.reportName
      path = descriptor.reportPath.toString()
      isQodana = descriptor.isQodanaReport
    }
  }

  private fun reportDescriptorFromState(state: ReportDescriptorState): LocalReportDescriptor? {
    val path = state.path?.let { Path(it) }
    val reportGuid = state.reportGuid
    val reportId = state.reportId
    if (path == null || reportGuid == null || reportId == null) return null

    val fileReportDescriptor = FileReportDescriptor(path, state.isQodana, reportGuid, reportId, project)

    val isLocalRun = state.isLocalRun
    val publishedReportLink = state.publishedReportLink
    return when {
      !isLocalRun -> {
        fileReportDescriptor
      }
      isLocalRun && publishedReportLink == null -> {
        LocalRunNotPublishedReportDescriptor(fileReportDescriptor, notificationIfFileNotPresent = false)
      }
      isLocalRun && publishedReportLink != null -> {
        LocalRunPublishedReportDescriptor(fileReportDescriptor, publishedReportLink, notificationIfFileNotPresent = false)
      }
      else -> null
    }
  }

  class State(@XCollection val descriptions: List<ReportDescriptorState> = listOf())

  @Tag("ReportDescription")
  class ReportDescriptorState {
    @get:Attribute
    var reportGuid: String? = null

    @get:Attribute
    var reportId: String? = null

    @get:Attribute
    var path: String? = null

    @get:Attribute
    var isQodana: Boolean = true

    @get:Attribute
    var isLocalRun: Boolean = false

    @get:Attribute
    var publishedReportLink: String? = null
  }
}

