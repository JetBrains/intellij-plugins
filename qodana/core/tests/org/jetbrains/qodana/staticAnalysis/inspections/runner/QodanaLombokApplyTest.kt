package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import de.plushnikov.intellij.plugin.LombokTestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.FixesStrategy
import org.junit.Test

@TestDataPath("\$CONTENT_ROOT/testData/QodanaLombokApplyTest")
class QodanaLombokApplyTest: QodanaQuickFixesTestBase(FixesStrategy.APPLY) {
  override fun setUp() {
    super.setUp()
    runBlocking(Dispatchers.EDT) {
      writeAction {
        ModuleRootManager.getInstance(module).modifiableModel.apply {
          MavenDependencyUtil.addFromMaven(this, LombokTestUtil.LOMBOK_MAVEN_COORDINATES, false)
          commit()
        }
      }
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  @Test
  fun testFieldsWithGetter() {
    runTest("qodana.single:LombokGetterMayBeUsed")
  }
}