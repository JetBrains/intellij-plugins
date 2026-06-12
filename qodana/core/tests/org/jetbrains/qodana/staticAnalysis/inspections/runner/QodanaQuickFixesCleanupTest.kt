package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.readText

class QodanaQuickFixesCleanupTest : QodanaQuickFixesCommonTests(FixesStrategy.CLEANUP) {
  @Test
  fun testPointlessArithmeticCleanupIsStableAcrossRuns() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:PointlessArithmeticExpression"),
        disableSanityInspections = true,
        runPromoInspections = false,
      )
    }

    val sourceFile = Path.of(project.basePath!!, "test-module", "App.java")
    val expectedAfterCleanup = getTestDataPath("afterFixes/App.java").readText()

    runAnalysis()

    val textAfterFirstRun = sourceFile.readText()
    assertSameLines(expectedAfterCleanup, textAfterFirstRun)

    runAnalysis()

    assertSameLines(textAfterFirstRun, sourceFile.readText())
  }
}
