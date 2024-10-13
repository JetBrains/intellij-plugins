package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.LinkedCloudReportDescriptor
import org.jetbrains.qodana.cloud.project.LinkedLatestCloudReportDescriptor
import org.jetbrains.qodana.cloud.project.QodanaCloudProjectLinkService
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.protocol.OpenInIdeCloudReportDescriptor
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.StatsReportType

internal class OpenQodanaCloudReportAction : DumbAwareToggleAction() {
  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean {
    val project = e.project
    val linked = (project?.service<QodanaCloudProjectLinkService>()?.linkState?.value as? LinkState.Linked)
    if (linked == null) {
      e.presentation.isEnabledAndVisible = false
      return false
    }

    val projectName by lazy { getCloudProjectName(linked) }
    return when {
      project.isLinkedCloudReportSelected -> {
        e.presentation.text = QodanaBundle.message("qodana.linked.project.highlighted", projectName)
        true
      }
      project.isLinkedCloudReportLoading -> {
        e.presentation.text = QodanaBundle.message("qodana.linked.project.loading.data")
        true
      }
      else -> {
        e.presentation.text = QodanaBundle.message("qodana.linked.project.highlight", projectName)
        false
      }
    }
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val project = e.project ?: return
    val linked = QodanaCloudProjectLinkService.getInstance(project).linkState.value as? LinkState.Linked ?: return
    val highlightedReportService = QodanaHighlightedReportService.getInstance(project)
    when {
      project.isLinkedCloudReportSelected || project.isLinkedCloudReportLoading -> {
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          highlightedReportService.highlightReport(null)
          logHighlightStats(project, false)
        }
      }
      else -> {
        project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
          val cloudReportDescriptor = linked.cloudReportDescriptorBuilder.createReportDescriptor()
          highlightedReportService.highlightReport(cloudReportDescriptor)
          logHighlightStats(project, true)
        }
      }
    }
  }
}

private fun logHighlightStats(project: Project, isHighlighted: Boolean) {
  QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
    project,
    isHighlighted,
    StatsReportType.CLOUD,
    SourceHighlight.TOOLS_LIST
  )
}

private fun getCloudProjectName(linked: LinkState.Linked): String {
  val lastLoadedProjectProperties = linked.projectDataProvider.projectProperties.value.lastLoadedValue
  return lastLoadedProjectProperties?.asSuccess()?.name ?: linked.projectDataProvider.projectPrimaryData.id
}

private val Project.isLinkedCloudReportSelected: Boolean
  get() {
    val selected = QodanaHighlightedReportService.getInstance(this).highlightedReportState.value as? HighlightedReportState.Selected
                   ?: return false
    return selected.highlightedReportData.sourceReportDescriptor.isFromLinkedCloudProject
  }

private val Project.isLinkedCloudReportLoading: Boolean
  get() {
    val loading = QodanaHighlightedReportService.getInstance(this).highlightedReportState.value as? HighlightedReportState.Loading
                   ?: return false
    return loading.sourceReportDescriptor.isFromLinkedCloudProject
  }

private val ReportDescriptor.isFromLinkedCloudProject: Boolean
  get() {
    return this is LinkedCloudReportDescriptor ||
           this is LinkedLatestCloudReportDescriptor ||
           this is OpenInIdeCloudReportDescriptor && this.linkedState() != null
  }