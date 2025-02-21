// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.protocol

import com.intellij.ide.IdeBundle
import com.intellij.ide.impl.ProjectUtil
import com.intellij.navigation.NavigatorWithinProject
import com.intellij.navigation.ProtocolOpenProjectResult
import com.intellij.navigation.openProject
import com.intellij.openapi.application.JBProtocolCommand
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.IncorrectOperationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.frontendUrl
import org.jetbrains.qodana.cloud.hostsEqual
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.stats.OpenInIdeProtocol
import org.jetbrains.qodana.stats.OpenInIdeResult
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SetupCiDialogSource
import org.jetbrains.qodana.ui.ci.showSetupCIDialogOrWizardWithYaml
import org.jetbrains.qodana.ui.protocol.OpenInIdeLogInDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class OpenInIdeCloudParameters(
  val reportId: String,
  val projectId: String?,
  val projectName: String?,
  val cloudHost: String?,
  val activateCoverage: Boolean
)
data class OpenInIdeProblemParameters(
  val pathText: String,
  val message: String,
  val path: String,
  val line: Int,
  val column: Int,
  val markerLength: Int,
  val origin: String,
  val revisionId: String?,
  val inspectionId: String?,
  val inspectionName: String?,
  val inspectionCategory: String?,
  val severity: QodanaSeverity?
)

internal class JBProtocolQodanaCommand : JBProtocolCommand("qodana") {
  /**
   * The handler parses 'qodana' protocol command.
   * Used by Qodana frontend (`setupCi` – finish step in onboarding, `showMarker` – button "Open in IntelliJ IDEA")

   * #### Common parameters
   * - `project`: a name of a project in IDE; used to choose a project to open
   * - `origin`: a VCS clone URL; used to choose a project to open if the `name` parameter is not specified

   * #### `setupCi` target
   *
   * opens "Setup Qodana in CI" dialog
   *
   * URL example:
   * `jetbrains://idea/qodana/setupCi?origin=https://github.com/avafanasiev/mockito.git&project=MyProjectName`

   *  #### 'showMarker' target
   *
   * Parameters:
   * - `path`: a file to open in an editor, relative to the project root directory; format: `$path[:$line[:$column]]`
   * - `length`: a length of a marker, in characters
   * - `marker_revision`: a VCS revision on which this marker was obtained
   * - `message`: marker message
   * - `inspection_id`: inspection's id of problem; optional
   * - `inspection_name`: inspection's name of problem; optional
   * - `inspection_category`: inspection's category of problem; optional
   * - `severity`: Qodana problem severity, one of following: Critical, High, Moderate, Low, Info; optional
   * - `cloud_report_id`: id of cloud report, if present, IDE will download the report (and ask for log in); optional
   * - `cloud_project_id`: id of project from cloud, used in dialog UI only; optional
   * - `cloud_project_name`: name of cloud project, used in dialog UI only; optional
   * - `activate_coverage`: if coverage window should be activated, boolean; optional
   *
   * URL example:
   * `jetbrains://idea/qodana/showMarker?origin=https://github.com/mockito/mockito.git
   * &path=src/test/java/org/mockitousage/verification/BasicVerificationInOrderTest.java:14:10&length=5
   * &marker_revision=067ff2446ad08d49a634374d04dd0e6913e1259c&message=Condition%20is%20always%20true
   * &inspection_id=Unused&inspection_name=Unused_Code_Constructs&inspection_category=Genera&severity=High
   * &cloud_report_id=55fs41&cloud_project_id=94hf83&cloud_project_name=My_Cloud_Project`

   * #### Note
   *
   * We interpret `line:column` values in following manner:
   * - line: a 1-based line number in a file
   * - column: a 1-based position of a character in a line
   */
  override suspend fun execute(target: String?, parameters: Map<String, String>, fragment: String?): String? {
    return when (target) {
      "showMarker" -> navigateAndShowMarker(parameters)
      "setupCi" -> openProjectAndOpenSetupCiDialog(parameters)
      else -> IdeBundle.message("jb.protocol.unknown.target", target)
    }
  }

  private suspend fun openProjectAndOpenSetupCiDialog(parameters: Map<String, String>): String? {
    val protocolForStats = OpenInIdeProtocol.SETUP_CI
    val project = when(val openProjectResult = openProjectAndLogStats(parameters, protocolForStats)) {
      is ProtocolOpenProjectResult.Success -> openProjectResult.project
      is ProtocolOpenProjectResult.Error -> return openProjectResult.message
    }

    showSetupCIDialogOrWizardWithYaml(project, SetupCiDialogSource.CLOUD)
    QodanaPluginStatsCounterCollector.OPEN_IN_IDE.log(protocolForStats, OpenInIdeResult.SUCCESS)
    return null
  }

  private suspend fun navigateAndShowMarker(parameters: Map<String, String>): String? {
    val openProjectResult = openProjectAndLogStats(parameters, OpenInIdeProtocol.SHOW_MARKER)
    val project = when(openProjectResult) {
      is ProtocolOpenProjectResult.Success -> openProjectResult.project
      is ProtocolOpenProjectResult.Error -> return openProjectResult.message
    }

    val openInIdeProblemParameters = getOpenInIdeProblemParameters(parameters)
    val openInIdeCloudParameters = getOpenInIdeCloudParameters(parameters)

    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      when {
        openInIdeProblemParameters != null && openInIdeCloudParameters == null -> {
          highlightOpenInIdeOneMarker(project, openInIdeProblemParameters)
        }
        openInIdeCloudParameters != null -> {
          val userStateFlow = QodanaCloudStateService.getInstance().userState
          val userState = userStateFlow.value
          if (userState !is UserState.Authorized ||
              !hostsEqual(userState.frontendUrl.toExternalForm(), openInIdeCloudParameters.cloudHost)) {
            val isOkInDialog = openLogInDialogAndWaitForOk(openInIdeCloudParameters, project)
            if (!isOkInDialog) return@launch
          }
          val authorized = userStateFlow.value as? UserState.Authorized ?: return@launch
          highlightOpenInIdeCloudReport(
            project,
            authorized,
            openInIdeCloudParameters.reportId,
            openInIdeCloudParameters.activateCoverage,
            openInIdeProblemParameters
          )
        }
      }
    }
    return null
  }

  private fun getOpenInIdeProblemParameters(parameters: Map<String, String>): OpenInIdeProblemParameters? {
    val pathText = parameters["path"] ?: return null
    val (path, line, column) = NavigatorWithinProject.parseNavigationPath(pathText)
    return OpenInIdeProblemParameters(
      pathText = pathText,
      message = parameters["message"] ?: return null,
      path = path ?: return null,
      line = line?.toInt() ?: return null,
      column = column?.toInt() ?: return null,
      markerLength = parameters["length"]?.toInt() ?: return null,
      origin = parameters["origin"] ?: "Unknown",
      revisionId = parameters["marker_revision"],
      inspectionId = parameters["inspection_id"],
      inspectionName = parameters["inspection_name"],
      inspectionCategory = parameters["inspection_category"],
      severity = parameters["severity"]?.let { QodanaSeverity.values().firstOrNull { severity -> severity.toString().equals(it, ignoreCase = true) } }
    )
  }

  private fun getOpenInIdeCloudParameters(parameters: Map<String, String>): OpenInIdeCloudParameters? {
    val cloudReportId = parameters["cloud_report_id"] ?: return null
    return OpenInIdeCloudParameters(
      reportId = cloudReportId,
      projectId = parameters["cloud_project_id"],
      projectName = parameters["cloud_project_name"],
      cloudHost = parameters["cloud_host"],
      activateCoverage = parameters["activate_coverage"]?.toBoolean() == true,
    )
  }

  private suspend fun openLogInDialogAndWaitForOk(openInIdeCloudParameters: OpenInIdeCloudParameters, project: Project): Boolean {
    return withContext(QodanaDispatchers.Ui) {
      val dialog = OpenInIdeLogInDialog(openInIdeCloudParameters, project)
      launch {
        writeIntentReadAction {
          dialog.show()
        }
      }
      // can't use showAndGet because in tests dialog is non-modal and it doesn't work
      suspendCoroutine { continuation ->
        try {
          Disposer.register(dialog.disposable) {
            continuation.resume(Unit)
          }
        }
        catch(_ : IncorrectOperationException) {
          continuation.resume(Unit)
        }
      }
      dialog.isOK
    }
  }

  private suspend fun openProjectAndLogStats(
    parameters: Map<String, String>,
    protocol: OpenInIdeProtocol
  ): ProtocolOpenProjectResult {
    val openProjectResult = openProject(parameters)
    when(openProjectResult) {
      is ProtocolOpenProjectResult.Error -> {
        QodanaPluginStatsCounterCollector.OPEN_IN_IDE.log(protocol, OpenInIdeResult.FAILED_OPEN_PROJECT)
      }
      is ProtocolOpenProjectResult.Success -> {
        withContext(QodanaDispatchers.Ui) {
          ProjectUtil.focusProjectWindow(openProjectResult.project, stealFocusIfAppInactive = true)
        }
      }
    }
    return openProjectResult
  }
}
