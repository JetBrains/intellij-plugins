package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.refactoring.JSOptimizeImportTestBase
import com.intellij.lang.javascript.typescript.TypeScriptFormatterTest
import com.intellij.openapi.application.PathManager
import com.intellij.util.Consumer

class VueOptimizeImportTest : JSOptimizeImportTestBase() {
  override fun getDefaultExtension(): String = "vue"
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/optimize_import/"


  fun testVueSimpleOptimize() {
    TypeScriptFormatterTest.setTempSettings(JavaScriptFormatterTestBase.getProject(), Consumer { settings ->
      settings.FORCE_QUOTE_STYlE = true
      settings.USE_DOUBLE_QUOTES = true
    })

    JSTestUtils.testES6<RuntimeException>(project) {
      val name = getTestName(false)
      myFixture.configureByFiles(name + ".vue", name + "_2.ts")
      runOptimizeAction(defaultExtension)
    }
  }
}