package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.actions.OptimizeImportsAction
import com.intellij.ide.DataManager
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.refactoring.JSOptimizeImportFixture.jsOptimizeImportFixture
import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

@TestApplication
class VueOptimizeImportTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsOptimizeImportFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + "/optimize_import/", "vue"))

  @Test
  fun testVueSimpleOptimize() {
    JavaScriptFormatterTestBase.setTempSettings(js.project, JavaScriptSupportLoader.TYPESCRIPT, Consumer<JSCodeStyleSettings> { settings ->
      settings.FORCE_QUOTE_STYlE = true
      settings.USE_DOUBLE_QUOTES = true
    })

    val name = js.getTestName(false)
    js.fixture.configureByFiles("$name.vue", name + "_2.ts")
    js.runOptimizeAction(js.extension)
  }

  @Test
  fun testUnusedImportStatement() {
    js.fixture.configureByFiles("VueSimpleOptimize_2.ts")
    js.fixture.configureByText("UnusedImportStatement.vue",
                              """<script lang="ts">import {Foo, Foo2} from './VueSimpleOptimize_2';</script>""")
    timeoutRunBlocking {
      withContext(Dispatchers.EDT) {
        OptimizeImportsAction.actionPerformedImpl(DataManager.getInstance().getDataContext(js.fixture.editor.contentComponent))
        FileDocumentManager.getInstance().saveAllDocuments()
      }
    }
    js.fixture.checkResult("<script lang=\"ts\"></script>")
  }

  @Test
  fun testVueSimpleOptimizeWithType() {
    val name = js.getTestName(false)
    js.fixture.configureByFiles("$name.vue", name + "_2.ts")
    js.runOptimizeAction(js.extension)
  }
}
