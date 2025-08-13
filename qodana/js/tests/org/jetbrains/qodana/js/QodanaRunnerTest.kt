package org.jetbrains.qodana.js

import com.intellij.openapi.application.PluginPathManager
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.clones.index.HashFragmentIndex
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/test-data/QodanaRunnerTest")
class QodanaRunnerTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "js", "test-data")

  @Test
  fun testDuplicatedCodeInspection() = runBlocking {
    HashFragmentIndex.requestRebuild()
    invokeAndWaitIfNeeded {
      PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
      IndexingTestUtil.waitUntilIndexesAreReady(project)
      FileBasedIndex.getInstance().ensureUpToDate(HashFragmentIndex.NAME, project, GlobalSearchScope.projectScope(project))
    }
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:DuplicatedCode"),
        disableSanityInspections = true,
        runPromoInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `testEmbedded problem`(): Unit = runBlocking {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:CssInvalidHtmlTagReference"),
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}
