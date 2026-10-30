package org.jetbrains.qodana.dart

import com.intellij.openapi.application.writeAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.SarifReport
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaselineSource
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

internal class DartAnalyzeBaselineTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: MutableMap<String, RecordedDartAnalyzeResponse> = mutableMapOf(
    "lib" to readDartAnalyzeTestData("fingerprints.json"),
  )

  @Test
  fun `keeps fingerprints stable on retrigger and separates diagnostics at the same location`() {
    runAnalysis()
    assertEquals(2, manager.sarifRun.results.size)
    assertSarifResults()

    runAnalysis()
    assertEquals(2, manager.sarifRun.results.size)
    assertSarifResults()
  }

  @Test
  fun `matches reported Dart diagnostics against a saved baseline`() {
    runAnalysis()
    val initialFingerprints = resultsByRuleId().mapValues { fingerprints(it.value) }
    val baseline = outputBasePath.resolve("dart-baseline.sarif.json")
    Files.newBufferedWriter(baseline).use { writer ->
      SarifUtil.writeReport(writer, SarifReport().withRuns(listOf(manager.sarifRun)))
    }
    updateQodanaConfig { it.copy(baseline = baseline.toString(), baselineSource = QodanaBaselineSource.LOCAL_BASELINE, includeAbsent = true) }

    runAnalysis()

    val results = resultsByRuleId()
    assertTrue(results.values.all { it.baselineState == Result.BaselineState.UNCHANGED })
    assertEquals(initialFingerprints, results.mapValues { fingerprints(it.value) })
  }

  @Test
  fun `baseline keeps definitions for remaining and absent diagnostic rules`() {
    updateSource(getTestDataPath("baseline-before.dart").readText())
    analyzeResponses["lib"] = readDartAnalyzeTestData("baseline-before.json")
    runAnalysis()

    val ruleId = "dart_unused_local_variable"
    val initialResults = manager.sarifRun.results.filter { it.ruleId == ruleId }
    assertEquals(2, initialResults.size)
    val firstFingerprint = fingerprints(initialResults.single { it.singleLocation().physicalLocation.region.startLine == 2 })
    val baseline = outputBasePath.resolve("dart-baseline.sarif.json")
    Files.newBufferedWriter(baseline).use { writer ->
      SarifUtil.writeReport(writer, SarifReport().withRuns(listOf(manager.sarifRun)))
    }
    updateQodanaConfig { it.copy(baseline = baseline.toString(), baselineSource = QodanaBaselineSource.LOCAL_BASELINE, includeAbsent = true) }

    updateSource(getTestDataPath("baseline-after.dart").readText())
    analyzeResponses["lib"] = readDartAnalyzeTestData("baseline-after.json")
    runAnalysis()

    val results = manager.sarifRun.results.filter { it.ruleId == ruleId }
    assertEquals(2, results.size)
    val remaining = results.single { it.baselineState == Result.BaselineState.UNCHANGED }
    assertEquals(2, remaining.singleLocation().physicalLocation.region.startLine)
    assertNotEquals(firstFingerprint, fingerprints(remaining))
    val removed = results.single { it.baselineState == Result.BaselineState.ABSENT }
    assertEquals(3, removed.singleLocation().physicalLocation.region.startLine)

    val absentRuleId = "dart_undefined_identifier"
    assertEquals(3, manager.sarifRun.results.size)
    val absent = manager.sarifRun.results.single { it.ruleId == absentRuleId }
    assertEquals(Result.BaselineState.ABSENT, absent.baselineState)
    assertEquals(4, absent.singleLocation().physicalLocation.region.startLine)

    val components = listOf(manager.sarifRun.tool.driver) + manager.sarifRun.tool.extensions
    val component = components.single { component -> component.rules.any { it.id == "DartAnalyze" } }
    for (id in listOf(ruleId, absentRuleId)) {
      assertEquals(1, components.sumOf { component -> component.rules.count { it.id == id } })
      assertEquals("DartAnalyze", component.rules.single { it.id == id }.properties?.get("parentRuleId"))
    }
  }

  private fun updateSource(text: String): Unit = timeoutRunBlocking {
    val file = requireNotNull(VfsUtil.findFile(Path.of(project.basePath!!, "lib/main.dart"), false))
    writeAction { VfsUtil.saveText(file, text) }
  }

  private fun resultsByRuleId(): Map<String, Result> {
    val sarifResults = manager.sarifRun.results
    assertEquals("Expected exactly one SARIF result per inspection", 2, sarifResults.size)
    val results = sarifResults.associateBy { it.ruleId }
    assertEquals(
      setOf("dart_unused_local_variable", "dart_non_constant_identifier_names"),
      results.keys,
    )
    return results
  }

  private fun fingerprints(result: Result): Pair<String, String> {
    val partialFingerprints = requireNotNull(result.partialFingerprints)
    return requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 1)) to
           requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 2))
  }
}
