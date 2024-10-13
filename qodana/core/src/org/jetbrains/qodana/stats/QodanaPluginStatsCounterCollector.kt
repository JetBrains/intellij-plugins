package org.jetbrains.qodana.stats

import com.intellij.internal.statistic.InspectionUsageFUSCollector.InspectionToolValidator
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.cloud.project.LinkedCloudReportDescriptor
import org.jetbrains.qodana.cloud.project.LinkedLatestCloudReportDescriptor
import org.jetbrains.qodana.coverage.CoverageLanguage
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.reportDescriptorIfSelectedOrLoading
import org.jetbrains.qodana.protocol.OpenInIdeCloudReportDescriptor
import org.jetbrains.qodana.protocol.SingleMarkerReportDescriptor
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.report.FileReportDescriptor
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.run.LocalRunNotPublishedReportDescriptor
import org.jetbrains.qodana.run.LocalRunPublishedReportDescriptor
import org.jetbrains.qodana.ui.ci.EditYamlAndSetupCIWizardDialog
import org.jetbrains.qodana.ui.problemsView.tree.model.*
import org.jetbrains.qodana.ui.run.wizard.*

internal object QodanaPluginStatsCounterCollector : CounterUsagesCollector() {
  override fun getGroup() = GROUP

  private val GROUP = EventLogGroup("qodana.plugin", 9)

  // --------------------
  // Open in IDE
  // --------------------

  private val OPEN_IN_IDE_PROTOCOL = EventFields.Enum<OpenInIdeProtocol>("protocol")

  private val OPEN_IN_IDE_RESULT = EventFields.Enum<OpenInIdeResult>("state")

  @JvmField
  val OPEN_IN_IDE = GROUP.registerEvent("open_in_ide", OPEN_IN_IDE_PROTOCOL, OPEN_IN_IDE_RESULT)

  // --------------------
  // Report highlight/unhighlight actions
  // --------------------

  private val IS_HIGHLIGHT = EventFields.Boolean("is_highlight")

  val REPORT_TYPE = EventFields.Enum<StatsReportType>("report_type")

  private val SOURCE_HIGHLIGHT = EventFields.Enum<SourceHighlight>("source")

  @JvmField
  val UPDATE_HIGHLIGHTED_REPORT = GROUP.registerEvent("highlight_report", IS_HIGHLIGHT, REPORT_TYPE, SOURCE_HIGHLIGHT)

  // --------------------
  // Report problem status changes (fixed / present)
  // --------------------

  private val INSPECTION_ID = EventFields.StringValidatedByCustomRule<InspectionToolValidator> ("inspection_id")

  private val PROBLEM_STATUS = EventFields.Enum<ProblemStatus>("problem_status")

  @JvmField
  val STATUS_CHANGED = GROUP.registerEvent("problem_status_changed", INSPECTION_ID, PROBLEM_STATUS)

  // --------------------
  // Cloud link/unlink
  // --------------------

  val IS_LINK = EventFields.Boolean("is_link")

  private val SOURCE_LINK_STATE = EventFields.Enum<SourceLinkState>("source")

  @JvmField
  val UPDATE_CLOUD_LINK = GROUP.registerEvent("cloud_link", IS_LINK, SOURCE_LINK_STATE)

  // --------------------
  // Cloud user authorized state
  // --------------------

  val FIELD_USER_STATE = EventFields.Enum<StatsUserState>("user_state")

  private val SOURCE_USER_STATE = EventFields.Enum<SourceUserState>("source")

  @JvmField
  val UPDATE_CLOUD_USER_STATE = GROUP.registerEvent("cloud_user_state", FIELD_USER_STATE, SOURCE_USER_STATE)


  // --------------------
  // Some qodana tab actions which are not implemented via `AnAction` but logging is needed
  // --------------------

  private val PANEL_ACTIONS_FIELD = EventFields.Enum<PanelActions>("panel_action")

  @JvmField
  val PANEL_ACTIONS = GROUP.registerEvent("panel.action.executed", PANEL_ACTIONS_FIELD)

  // --------------------
  // Server-side analysis panel events (problem selection, problem/file navigation)
  // --------------------

  private val PROBLEM_VIEW_SELECTED_NODE_TYPE = EventFields.Enum<SelectedNodeType>("problem_view_selected_node")

  @JvmField
  val PROBLEM_SELECTED = GROUP.registerEvent("problem.view.node.opened",
                                             PROBLEM_VIEW_SELECTED_NODE_TYPE,
                                             EventFields.RoundedInt("problems_count"))

  private val PROBLEM_VIEW_NAVIGATED_NODE_TYPE = EventFields.Enum<SelectedNodeType>("problem_view_navigated_node")

  @JvmField
  val PROBLEM_NAVIGATED = GROUP.registerEvent("problem.view.node.navigated",
                                              PROBLEM_VIEW_NAVIGATED_NODE_TYPE,
                                              EventFields.RoundedInt("problems_count"))

  private val TAB_STATE_ACTION = EventFields.Enum<TabState>("tab_state_action")

  @JvmField
  val RUN_QODANA_ACTION = GROUP.registerEvent("problem.view.run.qodana.pressed", TAB_STATE_ACTION)

  @JvmField
  val LOGIN_ACTION = GROUP.registerEvent("problem.view.login.pressed", TAB_STATE_ACTION)

  @JvmField
  val LINK_PROJECT_ACTION = GROUP.registerEvent("problem.view.link.project.pressed", TAB_STATE_ACTION)

  private val LEARN_MORE_SOURCE = EventFields.Enum<LearnMoreSource>("learn_more_source")

  @JvmField
  val LEARN_MORE_PRESSED = GROUP.registerEvent("problem.view.learn.more.pressed", LEARN_MORE_SOURCE)

  // --------------------
  // Data about Link Project Dialog
  // --------------------

  private val CREATE_PROJECT_SOURCE = EventFields.Enum<CreateProjectSource>("learn_more_source")

  @JvmField
  val CREATE_PROJECT_PRESSED = GROUP.registerEvent("link.dialog.create.project.pressed", CREATE_PROJECT_SOURCE)

  // --------------------
  // Data about highlighted report (problems count, which report)
  // --------------------

  private val HIGHLIGHTED_REPORT_PROBLEMS_COUNT = EventFields.RoundedInt("problems_count")

  @JvmField
  val HIGHLIGHTED_REPORT_INFO = GROUP.registerEvent("report.data.highlighted", REPORT_TYPE, HIGHLIGHTED_REPORT_PROBLEMS_COUNT)

  // --------------------
  // Qodana in-IDE analysis state
  // --------------------

  private val ANALYSIS_STATE = EventFields.Enum<AnalysisState>("state")

  @JvmField
  val ANALYSIS_STEP = GROUP.registerEvent("analysis.step.finished", ANALYSIS_STATE, EventFields.DurationMs)

  // --------------------
  // Logging of Qodana wizards dialogs (how much time spent on each step, transition to next step)
  // --------------------

  private val ALL_QODANA_WIZARDS = listOf(
    RunQodanaWizard.WIZARD_ID,
    EditYamlAndSetupCIWizardDialog.WIZARD_ID
  )

  private val ALL_QODANA_WIZARD_STEPS = listOf(
    WelcomeRunQodanaStep.ID,
    EditYamlAndRunQodanaStep.ID,
    EditYamlBeforeSetupCIStep.ID,
    SetupCIStep.ID,
  )

  val QODANA_WIZARD_ID = EventFields.String("wizard", ALL_QODANA_WIZARDS)

  val QODANA_WIZARD_CURRENT_STEP_ID = EventFields.String("current_step", ALL_QODANA_WIZARD_STEPS)

  val QODANA_WIZARD_NEXT_STEP_ID = EventFields.String("new_step", ALL_QODANA_WIZARD_STEPS)

  val QODANA_WIZARD_TRANSITION_TYPE = EventFields.Enum<QodanaWizardTransition>("transition")

  @JvmField
  val QODANA_WIZARD_DIALOG_TRANSITION = GROUP.registerVarargEvent(
    "wizard.dialog.step.finished",
    QODANA_WIZARD_ID,
    QODANA_WIZARD_CURRENT_STEP_ID,
    QODANA_WIZARD_NEXT_STEP_ID,
    QODANA_WIZARD_TRANSITION_TYPE,
    EventFields.DurationMs
  )

  // --------------------
  // Selection of "Static Analysis" tab in problems toolwindow
  // --------------------

  private val TAB_STATE = EventFields.Enum<TabState>("tab_state")

  @JvmField
  val TAB_SELECTED = GROUP.registerEvent("tab.selected", TAB_STATE)

  @JvmField
  val TAB_UNSELECTED = GROUP.registerEvent("tab.unselected", TAB_STATE, EventFields.DurationMs)

  // --------------------
  // Run dialog settings on starting of Qodana analysis
  // --------------------

  private val RUN_DIALOG_PUBLISH_CLOUD = EventFields.Boolean("publish_cloud")

  private val RUN_DIALOG_YAML_STATE = EventFields.Enum<RunDialogYamlState>("yaml")

  private val RUN_DIALOG_USE_BASELINE = EventFields.Boolean("with_baseline")

  @JvmField
  val RUN_DIALOG_START_RUN = GROUP.registerEvent(
    "run.dialog.started",
    RUN_DIALOG_PUBLISH_CLOUD,
    RUN_DIALOG_YAML_STATE,
    RUN_DIALOG_USE_BASELINE
  )

  // --------------------
  // When finishing "Setup CI", log with which CI is finished
  // --------------------

  private val SETUP_CI_PROVIDER = EventFields.Enum<SetupCiProvider>("ci")

  @JvmField
  val SETUP_CI_DIALOG_FINISHED = GROUP.registerEvent("setup.ci.finished", SETUP_CI_PROVIDER)

  private val SETUP_CI_DIALOG_SOURCE = EventFields.Enum<SetupCiDialogSource>("source")

  @JvmField
  val SETUP_CI_DIALOG_OPENED = GROUP.registerEvent("setup.ci.opened", SETUP_CI_DIALOG_SOURCE)

  // --------------------
  // Report with coverage was received (for all incoming Qodana reports), language and default visibility setting
  // --------------------

  @JvmField
  val REPORT_WITH_COVERAGE_RECEIVED = GROUP.registerEvent("report.with.coverage.received",
                                                          EventFields.Boolean("is_received"),
                                                          EventFields.StringList("language", CoverageLanguage.values().map { it.name }),
                                                          EventFields.Boolean("should_show"))
}

internal enum class OpenInIdeProtocol {
  SHOW_MARKER,
  SETUP_CI
}

internal enum class OpenInIdeResult {
  FAILED_OPEN_PROJECT,
  SUCCESS,
}

internal enum class StatsReportType {
  FILE,
  OPEN_IN_IDE,
  OPEN_IN_IDE_CLOUD_REPORT,
  CLOUD,
  LOCAL_RUN_NOT_PUBLISHED,
  LOCAL_RUN_PUBLISHED,
  UNKNOWN,
  NONE,
}

internal enum class SourceHighlight {
  // sources to highlight
  TOOLS_SELECT_SARIF_FILE,
  OPEN_IN_IDE,
  CLOUD_HIGHLIGHT_ON_LINK,
  CLOUD_HIGHLIGHT_NEW_REPORT_APPEARED_NOTIFICATION,
  CLOUD_AUTO_LOAD_LATEST,
  CLOUD_REFRESH_ACTION_PANEL,
  RUN_QODANA_DIALOG,
  PROBLEMS_VIEW_OPEN_REPORT,

  // sources to unhighlight
  REPORT_NOT_AVAILABLE,
  QODANA_PANEL_CANCEL_LOADING,
  EDITOR_INTENTION,
  CLOSE_ACTION_PANEL,

  // both
  TOOLS_LIST,
  SARIF_FILE,
}

enum class SourceLinkState {
  // sources to link
  AUTO_LINK,

  // sources to unlink
  UNAUTHORIZED,
  TOOLS_LIST,

  // both
  LINK_VIEW,
}

internal enum class StatsUserState {
  AUTHORIZED,
  AUTHORIZING,
  NOT_AUTHORIZED,
}

internal enum class SourceUserState {
  // sources to unauthorize
  REFRESH_TOKEN_EXPIRED,

  // sources to authorize
  OAUTH_SUCCEEDED,

  // sources to start authorizing/unauthorize
  QODANA_SETTINGS_PANEL,
  OPEN_IN_IDE_DIALOG,
}

internal enum class PanelActions {
  OPEN_QODANA_BROWSER_UI_FROM_BANNER,
  CLOSE_BANNER,
}

internal enum class SelectedNodeType {
  SEVERITY,
  INSPECTION_CATEGORY,
  INSPECTION,
  MODULE,
  DIRECTORY,
  FILE,
  PROBLEM,
  ROOT,
  OTHER,
}

internal enum class AnalysisState {
  STARTED,
  SUCCEEDED,
  CANCELLED,
  FAILED,
}

internal enum class QodanaWizardTransition {
  OPEN,
  NEXT,
  PREVIOUS,
  CLOSE,
}

internal enum class TabState {
    ANALYZING,
    AUTHORIZING,
    LOADING_REPORT,
    SELECTED_REPORT,
    NOT_AUTHORIZED_NO_CI,
    NOT_AUTHORIZED_CI_PRESENT,
    AUTHORIZED_NOT_LINKED_NO_CI,
    AUTHORIZED_NOT_LINKED_PRESENT,
    AUTHORIZED_LINKED_NO_CI,
    AUTHORIZED_LINKED_CI_PRESENT,
    OTHER
}

internal enum class RunDialogYamlState {
  SAVE,
  NO_SAVE,
  ALREADY_PRESENT
}

internal enum class SetupCiProvider {
    GITHUB,
    GITLAB,
    TEAMCITY,
    JENKINS,
    AZURE,
    CIRCLECI,
    SPACE,
    BITBUCKET
}

internal enum class SetupCiDialogSource {
    CLOUD,
    LOCAL_REPORT,
    BANNER,
    TOOLS_LIST,
    PROBLEMS_VIEW_AUTHORIZED_NOT_LINKED,
    PROBLEMS_VIEW_AUTHORIZED_LINKED,
}

internal enum class LearnMoreSource {
    PROBLEMS_PANEL_LINK,
    TOOLTIP
}

internal enum class CreateProjectSource {
    NO_PROJECTS_VIEW,
    SOUTH_PANEL
}

internal enum class ProblemStatus {
  DISAPPEARED,
  APPEARED,
  FIXED,
  NOT_FIXED,
}

internal fun ReportDescriptor.toStatsReportType(): StatsReportType {
  return when (this) {
    is FileReportDescriptor -> StatsReportType.FILE
    is SingleMarkerReportDescriptor -> StatsReportType.OPEN_IN_IDE
    is OpenInIdeCloudReportDescriptor -> StatsReportType.OPEN_IN_IDE_CLOUD_REPORT
    is LocalRunNotPublishedReportDescriptor -> StatsReportType.LOCAL_RUN_NOT_PUBLISHED
    is LocalRunPublishedReportDescriptor -> StatsReportType.LOCAL_RUN_PUBLISHED
    is LinkedLatestCloudReportDescriptor -> StatsReportType.CLOUD
    is LinkedCloudReportDescriptor -> StatsReportType.CLOUD
    else -> StatsReportType.UNKNOWN
  }
}

internal fun QodanaTreeNode<*, *, *>.toSelectedNodeType(): SelectedNodeType {
  return when (this) {
    is QodanaTreeSeverityNode -> SelectedNodeType.SEVERITY
    is QodanaTreeInspectionCategoryNode -> SelectedNodeType.INSPECTION_CATEGORY
    is QodanaTreeInspectionNode -> SelectedNodeType.INSPECTION
    is QodanaTreeModuleNode -> SelectedNodeType.MODULE
    is QodanaTreeDirectoryNode -> SelectedNodeType.DIRECTORY
    is QodanaTreeFileNode -> SelectedNodeType.FILE
    is QodanaTreeProblemNode -> SelectedNodeType.PROBLEM
    is QodanaTreeRoot -> SelectedNodeType.ROOT
    else -> SelectedNodeType.OTHER
  }
}

internal fun currentlyHighlightedReportStatsType(project: Project): StatsReportType {
  val highlightedReportState = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value
  val reportDescriptor = highlightedReportState.reportDescriptorIfSelectedOrLoading
  return reportDescriptor?.toStatsReportType() ?: StatsReportType.NONE
}

fun logCoverageReceivedStats(project: Project, isReceived: Boolean, languages: List<String>) {
  QodanaPluginStatsCounterCollector.REPORT_WITH_COVERAGE_RECEIVED.log(
    project,
    isReceived,
    languages,
    QodanaRegistry.openCoverageReportEnabled
  )
}