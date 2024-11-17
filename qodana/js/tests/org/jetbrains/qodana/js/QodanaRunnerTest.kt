package org.jetbrains.qodana.js

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.clones.index.HashFragmentIndex
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/QodanaRunnerTest")
class QodanaRunnerTest : QodanaRunnerTestCase() {
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
        profile = QodanaProfileConfig(name = "qodana.single:DuplicatedCode"),
        disableSanityInspections = true,
        runPromoInspections = false
      )
    }
    runAnalysis()
    assertSarifResults()
  }
}
