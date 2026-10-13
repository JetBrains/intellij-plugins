package org.jetbrains.qodana.jvm.kotlin

import com.intellij.openapi.application.PluginPathManager
import com.intellij.testFramework.TestDataPath
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaQuickFixesTestBase
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

@TestDataPath($$"$CONTENT_ROOT/../test-data/KotlinQuickFixesApplyTest")
class KotlinQuickFixesApplyTest : QodanaQuickFixesTestBase(FixesStrategy.APPLY) {
  override val testData: Path = Paths.get(PluginPathManager.getPluginHomePath("qodana"), "jvm", "kotlin", "test-data")

  @Test
  fun testConstantConditionInNestedIf() {
    runTest("qodana.single:KotlinConstantConditions")
  }
}
