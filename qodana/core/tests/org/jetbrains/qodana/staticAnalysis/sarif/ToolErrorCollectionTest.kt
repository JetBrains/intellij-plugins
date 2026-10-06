package org.jetbrains.qodana.staticAnalysis.sarif

import com.google.gson.reflect.TypeToken
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.GlobalSimpleInspectionTool
import com.intellij.codeInspection.InspectionApplicationException
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.diagnostic.PluginException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.util.Disposer
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.registerOrReplaceServiceInstance
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Notification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.diogen.QodanaDiogenReporter
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.QodanaConfigureNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.ToolErrorInspectListener
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test
import java.nio.file.Paths
import kotlin.io.path.bufferedReader
import kotlin.io.path.div
import kotlin.io.path.pathString

@TestDataPath($$"$CONTENT_ROOT/testData/QodanaRunnerTest")
class ToolErrorCollectionTest : QodanaRunnerTestCase() {
  @Test
  fun localInspection() {
    val tool = LocalTool()
    registerTool(tool)
    runTest(tool)
  }

  @Test
  fun simpleGlobalInspection() {
    val tool = SimpleGlobalTool()
    registerGlobalTool(tool)
    runTest(tool)
  }

  @Test
  fun globalInspection() {
    val tool = GlobalTool()
    registerGlobalTool(tool)
    runTest(tool)
  }

  @Test
  fun maxErrorCount() {
    updateQodanaConfig { it.copy(maxRuntimeNotifications = 0) }
    val tool = LocalTool()
    registerTool(tool)
    runTest(tool)
  }

  @Test
  fun maxErrorCountNoSanityNotification() {
    updateQodanaConfig { it.copy(maxRuntimeNotifications = 0, disableSanityInspections = false) }
    val tool = LocalTool()
    registerTool(tool)
    runTest(tool)
  }

  @Test
  fun sanityReachedNotification() {
    manager.registerEmbeddedProfilesTestProvider()
    updateQodanaConfig { it.copy(maxRuntimeNotifications = 0, disableSanityInspections = false, moduleSuspendThreshold = 2) }
    val sanityTool = LocalSanityTool()
    registerTool(sanityTool)

    val emptyTool = LocalEmptyTool()
    registerTool(emptyTool)
    runTest(emptyTool)
  }

  /**
   * Reports of the very same failure are folded into one notification that counts them and lists every affected file.
   */
  @Test
  fun identicalFailuresAreReportedOnce() {
    val tool = ReplayingLocalTool()
    registerTool(tool)
    runAnalysisWith(tool)

    val notification = collectedNotifications().single()

    assertThat(notification.message.text).isEqualTo("Inspection ReplayingLocal failed")
    assertThat(notification.occurrenceCount).isEqualTo(3)
    assertThat(notification.locationUris)
      .containsExactlyInAnyOrder("test-module/pack/Bar.java", "test-module/pack/Baz.java", "test-module/pack/Foo.java")
  }

  @Test
  fun distinctFailuresAreKeptApart() {
    val tool = LocalTool()
    registerTool(tool)
    runAnalysisWith(tool)

    val notifications = collectedNotifications()

    assertThat(notifications).isNotEmpty()
    assertThat(notifications.map { it.message.text }.distinct()).containsExactly("Inspection SimpleLocal failed")
    // No notification may be a plain copy of another one in the same report.
    assertThat(notifications.map { it.message.text to it.exception?.message }).doesNotHaveDuplicates()
    // Every file is reported, and none of them twice.
    assertThat(notifications.sumOf { it.occurrenceCount }).isEqualTo(3)
    assertThat(notifications.flatMap { it.locationUris })
      .containsExactlyInAnyOrder("test-module/pack/Bar.java", "test-module/pack/Baz.java", "test-module/pack/Foo.java")
  }

  @Test
  fun `reports an enabled inspection failure to Diogen`() {
    val diogen = mutableListOf<IdeaLoggingEvent>()
    val failure = IllegalStateException("failed")
    withDiogenReporter(true, diogen) {
      ToolErrorInspectListener().inspectionFailed("ReplayingLocal", failure, null, project)
    }

    val event = diogen.single()
    assertThat(event.message).isEqualTo("Inspection ReplayingLocal failed")
    assertThat(event.throwable).isSameAs(failure)
    assertThat(event.attachments).isEmpty()
  }

  @Test
  fun `attributes a Diogen report to the plugin that threw`() {
    val diogen = mutableListOf<IdeaLoggingEvent>()
    val qodana = PluginId.getId("org.intellij.qodana")
    withDiogenReporter(true, diogen) {
      ToolErrorInspectListener().inspectionFailed("SampleInspection", PluginException("failed", qodana), null, project)
    }

    assertThat(diogen.single().problematicPluginInfo?.pluginId).isEqualTo(qodana)
  }

  @Test
  fun `does not report inspection failures when Diogen is not enabled`() {
    val diogen = mutableListOf<IdeaLoggingEvent>()
    withDiogenReporter(false, diogen) {
      ToolErrorInspectListener().inspectionFailed("SampleInspection", IllegalStateException("failed"), null, project)
    }
    assertThat(diogen).isEmpty()
  }

  @Test
  fun `does not report ignored inspection failures to Diogen`() {
    val diogen = mutableListOf<IdeaLoggingEvent>()
    withDiogenReporter(true, diogen) {
      val listener = ToolErrorInspectListener()
      listener.inspectionFailed("SampleInspection", CancellationException(), null, project)
      listener.inspectionFailed("SampleInspection", ProcessCanceledException(), null, project)
      listener.inspectionFailed("SampleInspection", IndexNotReadyException.create(), null, project)
    }
    assertThat(diogen).isEmpty()
  }

  @Test
  fun `does not report expected or memory inspection failures to Diogen`() {
    val diogen = mutableListOf<IdeaLoggingEvent>()
    withDiogenReporter(true, diogen) {
      val listener = ToolErrorInspectListener()
      listener.inspectionFailed("SampleInspection", InspectionApplicationException("invalid configuration"), null, project)
      listener.inspectionFailed("SampleInspection", QodanaException("expected Qodana failure"), null, project)
      listener.inspectionFailed("SampleInspection", RuntimeException(OutOfMemoryError("Java heap space")), null, project)
    }
    assertThat(diogen).isEmpty()
  }

  private fun runTest(tool: InspectionProfileEntry) {
    runAnalysisWith(tool)

    val actual = collectedNotifications()

    val expected = getTestDataPath("expected_notifications.json")
      .bufferedReader()
      .use { reader ->
        val gson = SarifUtil.createGson()
        gson.fromJson(reader, object : TypeToken<List<Notification>>() {})
      }

    assertThat(actual).hasSameSizeAs(expected)
    actual.zip(expected).forEach { (a, e) ->
      assertThat(a.message).isEqualTo(e.message)
      assertThat(a.level).isEqualTo(e.level)
      assertThat(a.timeUtc).isNotNull()
      // Modify paths so they same as in os
      e.locations?.forEach {
        val uri = it.physicalLocation.artifactLocation.uri
        it.physicalLocation.artifactLocation.uri = Paths.get(uri).pathString
      }
      assertThat(a.locations).isEqualTo(e.locations)
      assertThat(a.properties).isEqualTo(e.properties)

      if (a.qodanaKind != SanityInspectionGroup.SANITY_FAILURE_NOTIFICATION) {
        assertThat(a.exception.message).isNotNull()
        // asserting more details on the stack trace will break when any piece of code in the stack changes
        assertThat(a.exception.message.lines()).hasSizeGreaterThan(10)
      }
    }
  }

  private fun runAnalysisWith(tool: InspectionProfileEntry) {
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:${tool.shortName}"),
      )
    }

    runBeforeAnalysis{ config, project -> QodanaConfigureNotificationCollector().configureForQodana(config, project) }
    LoggedErrorProcessor.executeWith<Nothing>(TestAwareErrorProcessor, ::runAnalysis)
  }

  private fun collectedNotifications(): List<Notification> =
    SarifUtil.readReport(qodanaConfig.outPath / FULL_SARIF_REPORT_NAME)
      .runs.orEmpty()
      .flatMap { run -> run.invocations.orEmpty() }
      .flatMap { it.toolExecutionNotifications.orEmpty() }

  private fun withDiogenReporter(enabled: Boolean, events: MutableList<IdeaLoggingEvent>, action: () -> Unit) {
    val disposable = Disposer.newDisposable()
    val scope = CoroutineScope(SupervisorJob())
    Disposer.register(disposable) { scope.cancel() }
    val service = QodanaDiogenReporter(scope) { event ->
      events.add(event)
      SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE)
    }
    ApplicationManager.getApplication().registerOrReplaceServiceInstance(QodanaDiogenReporter::class.java, service, disposable)
    service.start(enabled)
    try {
      action()
    }
    finally {
      runBlocking { service.stop() }
      Disposer.dispose(disposable)
    }
  }

  private val Notification.occurrenceCount: Int
    get() = (properties?.get(RuntimeNotificationCollector.OCCURRENCES_PROPERTY) as? Number)?.toInt() ?: 1

  /** The reported files, with separators normalized. */
  private val Notification.locationUris: List<String>
    get() = locations.orEmpty().map { it.physicalLocation.artifactLocation.uri.replace('\\', '/') }
}

private object TestAwareErrorProcessor : LoggedErrorProcessor() {
  const val TAG = "<<EXPECTED>>"

  override fun processError(category: String, message: String, details: Array<out String>, t: Throwable?): Set<Action> =
    if (t?.message?.contains(TAG) == true) Action.NONE else super.processError(category, message, details, t)
}

private class LocalEmptyTool : LocalInspectionTool() {
  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "EmptyLocal"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitClass(aClass: PsiClass) {
      }
    }
}

private class LocalSanityTool : LocalInspectionTool() {
  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "SimpleLocalSanity"

  override fun getDisplayName(): String = "SimpleLocalSanity"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitMethod(method: PsiMethod) {
        holder.registerProblem(method, "sanity")
      }

      override fun visitClass(aClass: PsiClass) {
        holder.registerProblem(aClass, "sanity")
      }
    }
}

private class LocalTool : LocalInspectionTool() {
  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "SimpleLocal"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitClass(aClass: PsiClass) {
        error(TestAwareErrorProcessor.TAG)
      }
    }
}

private class ReplayingLocalTool : LocalInspectionTool() {
  private val failure by lazy { IllegalStateException(TestAwareErrorProcessor.TAG) }

  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "ReplayingLocal"

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : JavaElementVisitor() {
      override fun visitClass(aClass: PsiClass) {
        throw failure
      }
    }
}

private class SimpleGlobalTool : GlobalSimpleInspectionTool() {

  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "SimpleGlobal"

  override fun checkFile(psiFile: PsiFile,
                         manager: InspectionManager,
                         problemsHolder: ProblemsHolder,
                         globalContext: GlobalInspectionContext,
                         problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
    error(TestAwareErrorProcessor.TAG)
  }
}

private class GlobalTool : GlobalInspectionTool() {

  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "Global"

  override fun runInspection(scope: AnalysisScope,
                             manager: InspectionManager,
                             globalContext: GlobalInspectionContext,
                             problemDescriptionsProcessor: ProblemDescriptionsProcessor) {
    error(TestAwareErrorProcessor.TAG)
  }

  override fun isReadActionNeeded(): Boolean = false

  override fun isGraphNeeded(): Boolean = false
}