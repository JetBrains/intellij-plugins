package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.refactoring.JSOptimizeImportTestBase
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.util.Consumer

class VueOptimizeImportTest : JSOptimizeImportTestBase() {
  override fun getDefaultExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath() + "/optimize_import/"


  fun testVueSimpleOptimize() {
    JavaScriptFormatterTestBase.setTempSettings(project, JavaScriptSupportLoader.TYPESCRIPT, Consumer<JSCodeStyleSettings> { settings ->
      settings.FORCE_QUOTE_STYlE = true
      settings.USE_DOUBLE_QUOTES = true
    })

    val name = getTestName(false)
    myFixture.configureByFiles("$name.vue", name + "_2.ts")
    runOptimizeAction(defaultExtension)
  }

  fun testUnusedImportStatement() {
    myFixture.configureByFiles("VueSimpleOptimize_2.ts")
    myFixture.configureByText("UnusedImportStatement.vue",
                              """<script lang="ts">import {Foo, Foo2} from './VueSimpleOptimize_2';</script>""")
    OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(myFixture.editor.contentComponent))
    FileDocumentManager.getInstance().saveAllDocuments()
    myFixture.checkResult("<script lang=\"ts\"></script>")
  }
  
  
  fun testVueSimpleOptimizeWithType() {
    val name = getTestName(false)
    myFixture.configureByFiles("$name.vue", name + "_2.ts")
    runOptimizeAction(defaultExtension)
  }
}
