package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.refactoring.JSOptimizeImportTestBase
import com.intellij.openapi.application.PathManager

class VueOptimizeImportTest: JSOptimizeImportTestBase() {
  override fun getDefaultExtension(): String = "vue"
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/testData/optimize_import/"
  
  
  fun testVueSimpleOptimize() {
    JSTestUtils.testES6<RuntimeException>(project) {
      val name = getTestName(false)
      myFixture.configureByFiles(name + ".vue", name + "_2.ts")
      runOptimizeAction(defaultExtension)
    }
  }
}