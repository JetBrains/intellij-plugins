package org.jetbrains.qodana.dart

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.PlatformTestUtil
import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.script.scoped.COVERAGE_SKIP_COMPUTATION_PROPERTY
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.writeText

internal class DartAnalyzeScopeQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val testDataBasePath: Path
    get() = Path.of(javaClass.simpleName, when (val testName = getTestName(true)) {
      "does not report diagnostics from an excluded Dart directory" -> "excludedDirectory"
      "does not report diagnostics from an excluded Dart file" -> "excludedFile"
      "reports a Dart diagnostic through SARIF" -> "diagnostics"
      "reports only changes limited by incremental scope" -> "incrementalScope"
      "reports diagnostics from root analysis options through SARIF" -> "rootDiagnostics"
      else -> testName
    })

  override val projectSourcePath: Path
    get() = when (getTestName(true)) {
      "reports diagnostics only from the selected directory",
      "does not report diagnostics from an excluded Dart file",
      "does not report diagnostics from an excluded Dart directory",
      "reports diagnostics only from explicitly included files" -> testData.resolve("dart-analyze/mini-monorepo")
      "reports diagnostics from root analysis options through SARIF" -> testData.resolve(testDataBasePath).resolve("sources/test-module")
      else -> testData.resolve("dart-analyze/project")
    }

  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse>
    get() = mapOf(
      "" to readDartAnalyzeTestData(
        when {
          projectSourcePath.endsWith("mini-monorepo") -> "monorepo-root.json"
          getTestName(true) == "reports only changes limited by incremental scope" -> "single-with-changed-file.json"
          getTestName(true) == "reports diagnostics from root analysis options through SARIF" -> "root-diagnostics.json"
          else -> "single-uncategorized.json"
        },
      ),
    )

  @Test
  fun `reports a Dart diagnostic through SARIF`() {
    runAnalysis()

    assertEquals(3, manager.sarifRun.results.size)
    assertSarifResults()
    assertEquals("DartAnalyze", sarifRulesById().getValue("dart_undefined_identifier").properties?.get("parentRuleId"))
  }

  @Test
  fun `reports diagnostics from root analysis options through SARIF`() {
    runAnalysis()

    val rootDiagnostic = manager.sarifRun.results.single { it.ruleId == "dart_undefined_lint" }
    assertEquals(4, manager.sarifRun.results.size)
    assertEquals(Level.WARNING, rootDiagnostic.level)
    assertEquals(
      "[undefined_lint] 'missing_lint_rule' isn't a recognized lint rule. Try using the name of a recognized lint rule.",
      rootDiagnostic.message.text,
    )
    assertEquals(
      listOf("analysis_options.yaml", "lib/main.dart", "lib/main.dart", "lib/main.dart"),
      sarifResultPaths(),
    )
    val region = rootDiagnostic.singleLocation().physicalLocation.region
    assertEquals(4, region.startLine)
    assertEquals(5, region.startColumn)
  }

  @Test
  fun `Dart diagnostics preserve the severity of each diagnostic type`() {
    runAnalysis()

    assertEquals(Level.ERROR, manager.sarifRun.results.single { it.ruleId == "dart_undefined_identifier" }.level)
    assertEquals(Level.WARNING, manager.sarifRun.results.single { it.ruleId == "dart_unused_local_variable" }.level)
    assertEquals(Level.NOTE, manager.sarifRun.results.single { it.ruleId == "dart_await_only_futures" }.level)
  }

  @Test
  fun `reports diagnostics only from the selected directory`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core")) }
    runAnalysis()

    assertEquals(
      listOf("packages/core/lib/main.dart", "packages/core/nested_flutter_app/lib/main.dart"),
      sarifResultPaths(),
    )
  }

  @Test
  fun `does not report diagnostics from an excluded Dart file`() {
    runAnalysis()

    assertEquals(
      listOf("apps/app/lib/main.dart", "packages/core/nested_flutter_app/lib/main.dart", "packages/flutter_app/lib/main.dart"),
      sarifResultPaths(),
    )
  }

  @Test
  fun `does not report diagnostics from an excluded Dart directory`() {
    runAnalysis()

    assertEquals(listOf("apps/app/lib/main.dart", "packages/flutter_app/lib/main.dart"), sarifResultPaths())
  }

  @Test
  fun `reports diagnostics only from explicitly included files`() {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope("DartAnalyze")),
        include = listOf(
          InspectScope("DartAnalyze", paths = listOf("apps/app/lib/main.dart")),
          InspectScope("DartAnalyze", paths = listOf("packages/core/lib/main.dart")),
        ),
      )
    }

    runAnalysis()

    assertEquals(listOf("apps/app/lib/main.dart", "packages/core/lib/main.dart"), sarifResultPaths())
  }

  @Test
  fun `reports only changes limited by incremental scope`() {
    val wanted = qodanaConfig.projectPath.resolve("lib/wanted.dart")
    wanted.writeText("void main() { missing; }\n")
    requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(wanted)).refresh(false, false)

    updateQodanaConfig {
      it.copy(script = QodanaScriptConfig(SCOPED_SCRIPT_NAME, mapOf("scope-file" to getTestDataPath("scope.json").toString())))
    }
    PlatformTestUtil.withSystemProperty<RuntimeException>(COVERAGE_SKIP_COMPUTATION_PROPERTY, "true") {
      runAnalysis()
    }

    assertEquals(1, manager.sarifRun.results.size)
    assertSarifResults()
  }

  @Test
  fun `analyzes the project directory regardless of the scope`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("lib")) }

    runAnalysis()

    assertRecordedAnalysisDirectories("")
  }

  private fun sarifRulesById() =
    (manager.sarifRun.tool.driver.rules + manager.sarifRun.tool.extensions.flatMap { it.rules }).associateBy { it.id }
}
