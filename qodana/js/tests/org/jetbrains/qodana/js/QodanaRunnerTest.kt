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
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/QodanaRunnerTest")
class QodanaRunnerTest : QodanaRunnerTestCase() {
  override val testData: Path = Path.of(PluginPathManager.getPluginHomePath("qodana"), "js", "test-data")

  @Test
  fun testDuplicatedCodeInspection() = runBlocking {
    buildHashFragmentIndex()
    useDuplicatedCodeProfile()
    runAnalysis()
    assertSarifResults()
  }

  /**
   * `Excluded.js` is out of the scope of the inspection, so its fragment has no row in the tool
   * result database. That one fragment must not remove the whole cluster.
   */
  @Test
  fun testDuplicatedCodeExcludedFragment() = runBlocking {
    buildHashFragmentIndex()
    useDuplicatedCodeProfile(exclude = listOf(InspectScope("DuplicatedCode", listOf("test-module/Excluded.js"))))
    runAnalysis()

    assertSarifResultLocations("test-module/App.js:3", "test-module/App.js:14")
  }

  /**
   * `Big.js` and `Other.js` hold the blocks P and Q, and `Small.js` holds only the block P.
   * The inspection drops the clone of P in the two larger files, because P is nested in the clone of
   * P and Q there. The aggregate report still names both fragments, so the report must keep them.
   */
  @Test
  fun testDuplicatedCodeNestedFragment() = runBlocking {
    buildHashFragmentIndex()
    useDuplicatedCodeProfile()
    runAnalysis()

    assertSarifResultLocations(
      "test-module/Big.js:2",
      "test-module/Big.js:3",
      "test-module/Other.js:2",
      "test-module/Other.js:3",
      "test-module/Small.js:3",
    )
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

  private fun useDuplicatedCodeProfile(exclude: List<InspectScope> = emptyList()) {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:DuplicatedCode"),
        exclude = exclude,
        disableSanityInspections = true,
        runPromoInspections = false
      )
    }
  }

  private fun buildHashFragmentIndex() {
    HashFragmentIndex.requestRebuild()
    invokeAndWaitIfNeeded {
      PlatformTestUtil.dispatchAllEventsInIdeEventQueue()
      IndexingTestUtil.waitUntilIndexesAreReady(project)
      FileBasedIndex.getInstance().ensureUpToDate(HashFragmentIndex.NAME, project, GlobalSearchScope.projectScope(project))
    }
  }
}
