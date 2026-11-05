package org.jetbrains.qodana.inspectionKts.js

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.inspectionKts.core.FORCE_DISABLE_INSPECTION_KTS
import org.jetbrains.qodana.inspectionKts.templates.InspectionKtsTemplate
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.jetbrains.qodana.staticAnalysis.withSystemProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

// The contract with Qodana CLI, spelled out to pin the names.
private const val INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY = "qodana.internal.inspections.kts.dir"
private const val INTERNAL_INSPECTIONS_RESULTS_KEY = "qodana.internal.results"
private const val INTERNAL_INSPECTIONS_ID_PREFIX = "qodana.internal."

@TestDataPath($$"$CONTENT_ROOT/../test-data/QodanaJsInspectionKtsTest")
class QodanaJsInspectionKtsTest : QodanaRunnerTestCase() {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "inspectionKts", "js", "test-data")

  @Test
  fun `testTemplate provides valid inspection`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, filename, inspectionKtsContent) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("qodana.single:MyJsInspection"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)
      reinstantiateKtsServices(project, testRootDisposable)

      runAnalysis()

      assertSarifResults()
    }
  }

  @Test
  fun `testInspection works in flexinspect subdir`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, "js/$filename", inspectionKtsContent) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("qodana.single:MyJsInspection"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)
      reinstantiateKtsServices(project, testRootDisposable)

      runAnalysis()

      assertSarifResults()
    }
  }

  @Test
  fun `testInternal inspection results stay out of report`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"

    withInternalInspectionKtsFile(filename, inspectionKtsTemplate.templateContent.invoke(filename)) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("empty"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)
      reinstantiateKtsServices(project, testRootDisposable)

      runAnalysis()
    }

    assertEmpty(manager.sarifRun.results.orEmpty())
    assertOnlyInternalResults(manager.sarifRun)
  }

  @Test
  fun `testInternal inspection stays internal in recommended profile`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"

    withInternalInspectionKtsFile(filename, inspectionKtsTemplate.templateContent.invoke(filename)) {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("qodana.recommended"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)
      reinstantiateKtsServices(project, testRootDisposable)

      runAnalysis()
    }

    // `qodana.recommended` enables FlexInspect inspections, the internal one included, but Qodana runs it in the internal group only.
    assertOnlyInternalResults(manager.sarifRun)
  }

  @Test
  fun `testInternal inspection does not clash with project one of same id`(): Unit = QodanaTestCase.runTest {
    val inspectionKtsTemplate = InspectionKtsTemplate.Provider.templates().first { it.uiDescriptor.id == JsInspectionKtsTemplateProvider.ID }
    val filename = "my-js-inspection"
    val inspectionKtsContent = inspectionKtsTemplate.templateContent.invoke(filename)

    withInspectionKtsFile(qodanaConfig.projectPath, filename, inspectionKtsContent) {
      withInternalInspectionKtsFile(filename, inspectionKtsContent) {
        updateQodanaConfig {
          it.copy(
            profile = QodanaProfileConfig.named("qodana.single:MyJsInspection"),
          )
        }
        reinstantiateInspectionRelatedServices(project, testRootDisposable)
        reinstantiateKtsServices(project, testRootDisposable)

        runAnalysis()
      }
    }

    val run = manager.sarifRun
    assertEquals(List(3) { "MyJsInspection" }, run.results.orEmpty().map { it.ruleId })
    assertOnlyInternalResults(run)
  }

  @Test
  fun `testInternal inspection compilation failure does not break analysis`(): Unit = QodanaTestCase.runTest {
    withInternalInspectionKtsFile("broken", "this is not kotlin") {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("empty"),
        )
      }
      reinstantiateInspectionRelatedServices(project, testRootDisposable)
      reinstantiateKtsServices(project, testRootDisposable)

      runAnalysis()
    }

    assertSarifExitCode(0)
    assertNull(manager.sarifRun.properties?.get(INTERNAL_INSPECTIONS_RESULTS_KEY))
  }

  @Test
  fun `testDisabled flexInspect doesnt hang`(): Unit = QodanaTestCase.runTest {
    withSystemProperty(FORCE_DISABLE_INSPECTION_KTS, "true") {
      updateQodanaConfig {
        it.copy(
          profile = QodanaProfileConfig.named("empty"),
        )
      }
      runAnalysis()
    }
  }
}

private fun assertOnlyInternalResults(run: Run) {
  val internalId = INTERNAL_INSPECTIONS_ID_PREFIX + "MyJsInspection"
  assertFalse(run.results.orEmpty().any { it.ruleId == internalId })
  val rules = (listOfNotNull(run.tool.driver) + run.tool.extensions.orEmpty()).flatMap { it.rules.orEmpty() }
  assertFalse(rules.any { it.id == internalId })
  val internalResults = run.properties?.get(INTERNAL_INSPECTIONS_RESULTS_KEY) as? List<*>
  // The same three problems in App.js that `testInspection works in flexinspect subdir` reports.
  assertEquals(List(3) { internalId }, internalResults.orEmpty().map { (it as Result).ruleId })
}

@OptIn(ExperimentalPathApi::class)
private suspend fun withInternalInspectionKtsFile(filename: String, content: String, action: suspend () -> Unit) {
  val directory = createTempDirectory("internal-inspections")
  try {
    directory.resolve("$filename.inspection.kts").writeText(content)
    withSystemProperty(INTERNAL_INSPECTIONS_KTS_DIR_PROPERTY, directory.toString()) {
      action()
    }
  }
  finally {
    directory.deleteRecursively()
  }
}
