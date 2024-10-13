package org.jetbrains.qodana.staticAnalysis.sarif

import com.google.gson.reflect.TypeToken
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.*
import com.intellij.psi.*
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.TestDataPath
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Notification
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.profile.SanityInspectionGroup
import org.junit.Test
import java.nio.file.Paths
import kotlin.io.path.bufferedReader
import kotlin.io.path.div
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
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

  private fun runTest(tool: InspectionProfileEntry) {
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig(name = "qodana.single:${tool.shortName}"),
      )
    }

    LoggedErrorProcessor.executeWith<Nothing>(TestAwareErrorProcessor, ::runAnalysis)

    val actual = SarifUtil.readReport(qodanaConfig.outPath / FULL_SARIF_REPORT_NAME)
      .runs.orEmpty()
      .flatMap { run -> run.invocations.orEmpty() }
      .flatMap { it.toolExecutionNotifications.orEmpty() }

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

private class SimpleGlobalTool : GlobalSimpleInspectionTool() {

  override fun getGroupDisplayName(): String = "TestGroup"

  override fun getShortName(): String = "SimpleGlobal"

  override fun checkFile(file: PsiFile,
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