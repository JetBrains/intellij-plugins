package org.jetbrains.qodana.protocol

import com.intellij.ide.impl.ProjectOriginInfoProvider
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.JBProtocolCommand
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.EditorTestUtil
import com.intellij.util.Urls
import com.intellij.util.application
import com.intellij.util.io.DigestUtil
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthService
import org.jetbrains.qodana.cloud.project.*
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.getDocument
import org.jetbrains.qodana.highlight.HighlightedReportData
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.highlight.highlightedReportDataIfSelected
import org.jetbrains.qodana.problem.SarifProblem
import org.jetbrains.qodana.registerDialogInterceptor
import org.jetbrains.qodana.reinstansiateService
import org.jetbrains.qodana.staticAnalysis.sarif.QodanaSeverity
import org.jetbrains.qodana.ui.ci.EditYamlAndSetupCIWizardDialog
import org.jetbrains.qodana.ui.ci.SetupCIDialog
import org.jetbrains.qodana.ui.protocol.OpenInIdeLogInDialog
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

private const val GIT_ORIGIN = "ssh://user@example.com:example.git"
private val GIT_ORIGIN_URL_ENCODED = URLEncoder.encode(GIT_ORIGIN, StandardCharsets.UTF_8)

class JBProtocolShowQodanaReportCommandTest: QodanaPluginHeavyTestBase() {
  override fun runInDispatchThread() = false

  override fun getBasePath() = Path(super.getBasePath(), "navigationProtocols").pathString

  override fun setUp() {
    super.setUp()
    copyProjectTestData("project")
    reinstansiateService(project, QodanaHighlightedReportService(project, scope))
    reinstansiateService<QodanaCloudOAuthService>(application, QodanaCloudOAuthServiceMock)
    reinstansiateService(application, QodanaCloudStateService(scope))
    reinstansiateService(project, QodanaCloudProjectLinkService(project, scope))
  }

  fun `test showMarker at text with space indent by vcs origin`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker at text with space indent by project name`(): Unit = runBlocking {
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "project=${project.name}&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker at text with tabulation indent by vcs origin`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(12, 3, 6)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtTabulationIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker at text with tabulation indent by project name`(): Unit = runBlocking {
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(12, 3, 6)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "project=${project.name}&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtTabulationIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker with inspectionId`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (inspectionId) = listOf("inspection")

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&inspection_id=$inspectionId"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertThat(it.inspectionsInfoProvider.getName(inspectionId)).isEqualTo("")
      assertThat(it.inspectionsInfoProvider.getCategory(inspectionId)).isEqualTo("")
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo(inspectionId)
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker with inspectionId and inspectionName`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (inspectionId, inspectionName) = listOf("inspection", "inspectionName")

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&inspection_id=$inspectionId&inspection_name=$inspectionName"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertThat(it.inspectionsInfoProvider.getName(inspectionId)).isEqualTo(inspectionName)
      assertThat(it.inspectionsInfoProvider.getCategory(inspectionId)).isEqualTo("")
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo(inspectionId)
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker with inspectionId and inspectionName and inspectionCategory`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (inspectionId, inspectionName, inspectionCategory) = listOf("inspection", "inspectionName", "inspectionCategory")

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&inspection_id=$inspectionId&inspection_name=$inspectionName&inspection_category=$inspectionCategory"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertThat(it.inspectionsInfoProvider.getName(inspectionId)).isEqualTo(inspectionName)
      assertThat(it.inspectionsInfoProvider.getCategory(inspectionId)).isEqualTo(inspectionCategory)
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo(inspectionId)
        assertThat(problem.qodanaSeverity).isEqualTo(QodanaSeverity.HIGH)
      }
    }
  }

  fun `test showMarker with info severity`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.INFO)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&severity=$severity"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker with low severity`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.LOW)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&severity=$severity"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker with moderate severity`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.MODERATE)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&severity=$severity"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker with high severity`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.HIGH)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&severity=$severity"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker with critical severity`(): Unit = runBlocking {
    registerProjectOriginProvider()
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.CRITICAL)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision" +
      "&severity=$severity"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker by region hash`(): Unit = runBlocking {
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.HIGH)

    val hash = sha256("//code")
    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision&severity=$severity" +
      "&hashed_region=$path:$line:$column:$length&region_hash_method=sha256&region_hash=$hash"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker by region only file`(): Unit = runBlocking {
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.HIGH)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision&severity=$severity" +
      "&hashed_region=$path"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
    checkHighlightedReportData {
      assertSingleSarifProblem(it) { problem ->
        assertThat(problem.relativePathToFile).isEqualTo(path)
        assertThat(problem.startLine).isEqualTo(line - 1)
        assertThat(problem.startColumn).isEqualTo(column - 1)
        assertThat(problem.charLength).isEqualTo(length)
        assertThat(problem.message).isEqualTo(message)
        assertThat(problem.revisionId).isEqualTo(revision)
        assertThat(problem.inspectionId).isEqualTo("")
        assertThat(problem.qodanaSeverity).isEqualTo(severity)
      }
    }
  }

  fun `test showMarker region hash wrong hash`(): Unit = runBlocking {
    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    val (severity) = listOf(QodanaSeverity.HIGH)

    val hash = sha256("wrong hash")
    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision&severity=$severity" +
      "&hashed_region=$path:$line:$column:$length&region_hash_method=sha256&region_hash=$hash"
    )

    val isTimeout = withTimeoutOrNull(5.seconds) {
      fileOpened.join()
    } == null

    assertThat(isTimeout).isTrue

    fileOpened.cancel()
  }

  fun `test showMarker only cloudReportId`(): Unit = runBlocking {
    val reportId = "reportId"
    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    val actualProjectId = "actualProjectId"
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, null, null, null, false))

    doTransitionToAuthorized()
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val activateCoverageUiTab = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).uiTabsActivateRequest.map { it.activateCoverage }.first()
    }
    assertThat(activateCoverageUiTab).isFalse()
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker cloudReportId and activateCoverage`(): Unit = runBlocking {
    val reportId = "reportId"
    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    val actualProjectId = "actualProjectId"
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&activate_coverage=true"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, null, null, null, true))

    doTransitionToAuthorized()
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val activateCoverageUiTab = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).uiTabsActivateRequest.map { it.activateCoverage }.first()
    }
    assertThat(activateCoverageUiTab).isTrue()
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker cloudReportId and cloudProjectId and cloudProjectName`(): Unit = runBlocking {
    val reportId = "reportId"
    val projectId = "projectId"
    val projectName = "projectName"
    val actualProjectId = "actualProjectId"

    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&cloud_project_id=$projectId&cloud_project_name=$projectName"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, projectId, projectName, null, false))

    doTransitionToAuthorized()
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }

    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker cloudReportId and cloudProjectId and cloudProjectName and cloudHost`(): Unit = runBlocking {
    val reportId = "reportId"
    val projectId = "projectId"
    val projectName = "projectName"
    val cloudHost = "https://cloud.host"

    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    val actualProjectId = "actualProjectId"
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&cloud_project_id=$projectId&cloud_project_name=$projectName" +
      "&cloud_host=$cloudHost"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, projectId, projectName, cloudHost, false))

    doTransitionToAuthorized(Urls.newFromEncoded(cloudHost))
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }

    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker already authorized`(): Unit = runBlocking {
    val reportId = "reportId"

    registerProjectOriginProvider()
    mockReportDownloadPath(reportId)
    val actualProjectId = "actualProjectId"
    mockCloudProjectData(actualProjectId, "organization_id", reportId)
    doTransitionToAuthorized()

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId"
    )

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker authorized cloud then on premise`(): Unit = runBlocking {
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    registerProjectOriginProvider()
    val cloudHost = "https://cloud.host"
    val actualProjectId = "actualProjectId"
    val reportId = "reportId"

    mockQDCloudHttpClient.respondReportFiles(reportId) { files, _ ->
      @Suppress("JsonStandardCompliance")
      val sarifPath = "file://${Path(testDataPath, "qodana.sarif.json").invariantSeparatorsPathString}"
      @Language("JSON")
      val emptyFilesResponse = """
        {
          "files": []
        }
      """.trimIndent()
      @Language("JSON")
      val fileResponse = """
        {
          "files": [
            {
              "file": "qodana.sarif.json",
              "url": "$sarifPath"
            }
          ]
        }
      """.trimIndent()
      qodanaCloudResponse {
        val authorized = (QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized)
        val isSelfHosted = authorized?.selfHostedFrontendUrl?.toExternalForm() == cloudHost

        if (!isSelfHosted || files.single() != "qodana.sarif.json") emptyFilesResponse else fileResponse
      }
    }

    mockCloudProjectData(actualProjectId, "organization_id", reportId)
    val authorized = doTransitionToAuthorized()

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&cloud_host=$cloudHost"
    )

    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }

    authorized.logOut()
    doTransitionToAuthorized(Urls.newFromEncoded(cloudHost))

    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker authorized on premise then cloud`(): Unit = runBlocking {
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    registerProjectOriginProvider()
    val actualProjectId = "actualProjectId"
    val reportId = "reportId"

    mockQDCloudHttpClient.respondReportFiles(reportId) { files, _ ->
      @Suppress("JsonStandardCompliance")
      val sarifPath = "file://${Path(testDataPath, "qodana.sarif.json").invariantSeparatorsPathString}"
      @Language("JSON")
      val emptyFilesResponse = """
        {
          "files": []
        }
      """.trimIndent()
      @Language("JSON")
      val fileResponse = """
        {
          "files": [
            {
              "file": "qodana.sarif.json",
              "url": "$sarifPath"
            }
          ]
        }
      """.trimIndent()
      qodanaCloudResponse {
        val authorized = (QodanaCloudStateService.getInstance().userState.value as? UserState.Authorized)
        val isSelfHosted = authorized?.selfHostedFrontendUrl?.toExternalForm() != null

        if (isSelfHosted || files.single() != "qodana.sarif.json") emptyFilesResponse else fileResponse
      }
    }
    mockCloudProjectData(actualProjectId, "organization_id", reportId)
    val authorized = doTransitionToAuthorized(Urls.newFromEncoded("https://cloud.host"))

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&cloud_host=${QodanaCloudDefaultUrls.websiteUrl}"
    )

    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }

    authorized.logOut()
    doTransitionToAuthorized()

    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker cloudReportId and problem parameters`(): Unit = runBlocking {
    val reportId = "reportId"
    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)
    val actualProjectId = "actualProjectId"

    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    val fileOpened = subscribeToFileOpenedEvent()

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&" +
      "path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, null, null, null, false))

    doTransitionToAuthorized()
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
    withTimeout(15.seconds) {
      fileOpened.join()
    }
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
  }

  fun `test showMarker first cloudReportId then cloudReportId with problem parameters`(): Unit = runBlocking {
    val reportId = "reportId"
    val actualProjectId = "actualProjectId"

    registerProjectOriginProvider()
    val dialogDeferred = registerDialogInterceptor<OpenInIdeLogInDialog>()
    mockReportDownloadPath(reportId)
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId"
    )
    val dialog = withTimeout(15.seconds) {
      dialogDeferred.await()
    }
    assertThat(dialog.openInIdeCloudParameters).isEqualTo(OpenInIdeCloudParameters(reportId, null, null, null, false))

    doTransitionToAuthorized()
    withContext(Dispatchers.EDT) {
      dialog.close(0)
    }

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }

    val fileOpened = subscribeToFileOpenedEvent()

    val (path, message, revision) = listOf("Main.java", "qodana", "latest")
    val (line, column, length) = listOf(8, 5, 6)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId&" +
      "path=$path:$line:$column&length=$length&message=$message&marker_revision=$revision"
    )

    withTimeout(15.seconds) {
      fileOpened.join()
    }
    val highlightedStateAfterSecondProtocolCall = QodanaHighlightedReportService.getInstance(project).highlightedReportState.value
    assertThat(highlightedStateAfterSecondProtocolCall).isSameAs(selected)
    checkCaretAndSelection("MainCaretAtSpaceIndent.java")
  }

  fun `test showMarker already authorized and linked`(): Unit = runBlocking {
    val reportId = "reportId"
    val actualProjectId = "actualProjectId"
    val actualOrganizationId = "organization_id"

    registerProjectOriginProvider()
    mockReportDownloadPath(reportId)
    val authorized = doTransitionToAuthorized()
    doTransitionToLinked(project, authorized, actualProjectId, actualOrganizationId)
    mockCloudReportsTimeline(actualProjectId)
    mockCloudProjectData(actualProjectId, actualOrganizationId, reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId"
    )

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    assertThat(reportDescriptor?.linkedState()).isNotNull
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test showMarker already authorized and linked to different project`(): Unit = runBlocking {
    val actualProjectId = "actualProjectId"
    val reportId = "reportId"

    registerProjectOriginProvider()
    mockReportDownloadPath(reportId)
    val authorized = doTransitionToAuthorized()
    doTransitionToLinked(project, authorized, "different_project_id", "different_organization_id")
    mockCloudReportsTimeline(actualProjectId)
    mockCloudProjectData(actualProjectId, "organization_id", reportId)

    executeProtocolCommand(
      "idea/qodana/showMarker?" +
      "origin=$GIT_ORIGIN_URL_ENCODED&cloud_report_id=$reportId"
    )

    val selected = withTimeout(15.seconds) {
      QodanaHighlightedReportService.getInstance(project).highlightedReportState
        .filterIsInstance<HighlightedReportState.Selected>()
        .first()
    }
    val reportDescriptor = selected.highlightedReportData.sourceReportDescriptor as? OpenInIdeCloudReportDescriptor
    assertThat(reportDescriptor?.reportId).isEqualTo(reportId)
    assertThat(reportDescriptor?.projectId).isEqualTo(actualProjectId)
    checkHighlightedReportData {
      assertThat(it.allProblems).isEqualTo(expectedProblemsFromSarifReport())
    }
  }

  fun `test setupCi by project name`(): Unit = runBlocking {
    doSetupCiTest<EditYamlAndSetupCIWizardDialog>("idea/qodana/setupCi?project=${project.name}")
  }

  fun `test setupCi by vcs origin`(): Unit = runBlocking {
    registerProjectOriginProvider()
    doSetupCiTest<EditYamlAndSetupCIWizardDialog>("idea/qodana/setupCi?origin=$GIT_ORIGIN_URL_ENCODED")
  }

  fun `test setupCi by vcs origin and project name`(): Unit = runBlocking {
    registerProjectOriginProvider()
    doSetupCiTest<EditYamlAndSetupCIWizardDialog>("idea/qodana/setupCi?project=${project.name}&origin=$GIT_ORIGIN_URL_ENCODED")
  }

  fun `test setupCi by project name with qodana yaml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yaml")
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?project=${project.name}")
  }

  fun `test setupCi by vcs origin with qodana yaml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yaml")
    registerProjectOriginProvider()
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?origin=$GIT_ORIGIN_URL_ENCODED")
  }

  fun `test setupCi by vcs origin and project name with qodana yaml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yaml")
    registerProjectOriginProvider()
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?project=${project.name}&origin=$GIT_ORIGIN_URL_ENCODED")
  }

  fun `test setupCi by project name with qodana yml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yml")
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?project=${project.name}")
  }

  fun `test setupCi by vcs origin with qodana yml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yml")
    registerProjectOriginProvider()
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?origin=$GIT_ORIGIN_URL_ENCODED")
  }

  fun `test setupCi by vcs origin and project name with qodana yml in project`(): Unit = runBlocking {
    myFixture.copyFileToProject("qodana.yml")
    registerProjectOriginProvider()
    doSetupCiTest<SetupCIDialog>("idea/qodana/setupCi?project=${project.name}&origin=$GIT_ORIGIN_URL_ENCODED")
  }

  private fun registerProjectOriginProvider() {
    val epName = ExtensionPointName.create<ProjectOriginInfoProvider>("com.intellij.projectOriginInfoProvider")
    epName.point.registerExtension(object : ProjectOriginInfoProvider {
      override fun getOriginUrl(projectDir: Path): String = GIT_ORIGIN
    }, testRootDisposable)
  }

  private fun CoroutineScope.subscribeToFileOpenedEvent(): Job {
    return launch {
      callbackFlow {
        val disposable = Disposer.newDisposable()
        project.messageBus.connect(disposable).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
          override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
            trySendBlocking(Unit)
          }
        })
        awaitClose { Disposer.dispose(disposable) }
      }.first()
    }
  }

  private suspend fun executeProtocolCommand(uri: String) = JBProtocolCommand.execute(uri)

  private suspend fun checkCaretAndSelection(pathToExpectedFileInTestData: String) {
    withContext(Dispatchers.EDT) {
      val openedFile = FileEditorManager.getInstance(project).allEditors.asSequence()
                         .filterIsInstance<TextEditor>()
                         .map { it.file }
                         .firstOrNull() ?: return@withContext
      //readaction is not enough
      writeIntentReadAction {
        myFixture.openFileInEditor(openedFile)
      }
      yield()
      //maybe readaction
      writeIntentReadAction {
        val expectedDocument = loadAdditionalTestDataFile(pathToExpectedFileInTestData).getDocument()!!
        EditorTestUtil.verifyCaretAndSelectionState(myFixture.editor, EditorTestUtil.extractCaretAndSelectionMarkers(expectedDocument))
      }
    }
  }

  private suspend fun checkHighlightedReportData(checkAction: suspend (HighlightedReportData) -> Unit) {
    val highlightedReportData = QodanaHighlightedReportService.getInstance(project)
      .highlightedReportState.value.highlightedReportDataIfSelected!!

    checkAction.invoke(highlightedReportData)
  }

  private fun assertSingleSarifProblem(highlightedReportData: HighlightedReportData, checkAction: (SarifProblem) -> Unit) {
    val problem = assertOneElement(highlightedReportData.allProblems)
    checkAction(problem)
  }

  private fun mockReportDownloadPath(reportId: String) {
    mockQDCloudHttpClient.respondReportFiles(reportId) { files, _ ->
      qodanaCloudResponse {
        @Language("JSON")
        val noFilesResponse = """
          {
            "files": []
          }
        """.trimIndent()
        if (files.single() != "qodana.sarif.json") return@qodanaCloudResponse noFilesResponse

        @Suppress("JsonStandardCompliance")
        val sarifPath = "file://${Path(testDataPath, "qodana.sarif.json").invariantSeparatorsPathString}"
        @Language("JSON")
        val sarifFileResponse = """
          {
            "files": [
              {
                "file": "qodana.sarif.json",
                "url": "$sarifPath"
              }
            ]
          }
        """.trimIndent()
        sarifFileResponse
      }
    }
  }

  private fun mockCloudProjectData(
    projectId: String,
    organizationId: String,
    reportId: String,
  ) {
    mockQDCloudHttpClient.respond("projects/$projectId") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
          {
            "id": "$projectId",
            "organizationId": "$organizationId",
            "name": "name"
          }
        """.trimIndent()
        response
      }
    }
    mockQDCloudHttpClient.respond("reports/$reportId") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
          {
            "projectId": "$projectId"
          }
        """.trimIndent()
        response
      }
    }
  }

  private fun mockCloudReportsTimeline(projectId: String) {
    mockQDCloudHttpClient.respond("projects/$projectId/timeline") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
          {
            "next": 0,
            "items": []
          }
        """.trimIndent()
        response
      }
    }
  }

  private suspend inline fun <reified T : DialogWrapper> doSetupCiTest(uri: String) {
    val dialog = registerDialogInterceptor<T>()

    executeProtocolCommand(uri)

    withContext(Dispatchers.EDT) {
      withTimeout(15.seconds) {
        dialog.await().close(0)
      }
    }
  }

  private fun expectedProblemsFromSarifReport(): Set<SarifProblem> {
    val problemAtSpaceIndent = SarifProblem(
      startLine = 7,
      startColumn = 4,
      endLine = null,
      endColumn = null,
      charLength = 6,
      message = "qodana",
      relativePathToFile = "Main.java",
      qodanaSeverity = QodanaSeverity.MODERATE,
      inspectionId = "inspection1",
      baselineState = Result.BaselineState.NEW,
      snippetText = null,
      revisionId = "latest"
    )
    val problemAtTabulationIndent = SarifProblem(
      startLine = 11,
      startColumn = 2,
      endLine = null,
      endColumn = null,
      charLength = 6,
      message = "qodana",
      relativePathToFile = "Main.java",
      qodanaSeverity = QodanaSeverity.CRITICAL,
      inspectionId = "inspection2",
      baselineState = Result.BaselineState.NEW,
      snippetText = null,
      revisionId = "latest"
    )
    return setOf(problemAtSpaceIndent, problemAtTabulationIndent)
  }
}

private fun sha256(region: String): String {
  val sha256 = DigestUtil.sha256()
  sha256.update(region.toByteArray())
  return DigestUtil.digestToHash(sha256)
}