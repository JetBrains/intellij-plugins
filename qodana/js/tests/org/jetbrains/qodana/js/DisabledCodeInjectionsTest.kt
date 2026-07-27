package org.jetbrains.qodana.js

import com.intellij.openapi.application.PluginPathManager
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path

class DisabledCodeInjectionsTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "core", "test-data")

  @Test
  fun `html in js strings disabled`(): Unit = runBlocking {
    runBeforeAnalysis { config, project ->
      QodanaDisableJsInjections().configureForQodana(config, project)
    }
    runAnalysis()
    assertSarifResults()
  }
}
