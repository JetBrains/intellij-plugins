package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test

internal class DartAnalyzeGlobalInspectionFailureTest : DartAnalyzeProjectQodanaTestCase() {
  override val preparationFailures: Map<String, String> = mapOf(
    "legacy-module" to "The lower bound of sdk must be 2.12.0 or higher to enable null safety.",
    "legacy-module-2" to "The lower bound of sdk must be 2.12.0 or higher to enable null safety.",
  )

  @Test
  fun `reports setup failures for each failed module without file locations`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME")) }
    runAnalysis()

    val results = manager.sarifRun.results
    assertEquals(2, results.size)
    assertTrue(results.all { it.ruleId == DART_PROJECT_SETUP_SHORT_NAME })
    assertTrue(results.all { it.level == Level.ERROR })
    assertTrue(results.all { it.message.text.contains("must be 2.12.0") })
    assertEquals(setOf("legacy-module", "legacy-module-2"), results.map { result ->
      when {
        result.message.text.contains("legacy-module-2") -> "legacy-module-2"
        result.message.text.contains("legacy-module") -> "legacy-module"
        else -> error("Missing module path in '${result.message.text}'")
      }
    }.toSet())
    assertTrue(results.all { result -> result.locations.all { it.physicalLocation == null } })
  }
}
