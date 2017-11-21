package org.jetbrains.vuejs.language

import com.intellij.lang.javascript.JavaScriptFormatterTest
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.typescript.TypeScriptFormatterTest
import com.intellij.util.Consumer

class VueFormatterTest : JavaScriptFormatterTestBase() {

  fun testTypeScriptTag() {
    TypeScriptFormatterTest.setTempSettings(getProject(), Consumer { settings ->
      settings.FORCE_QUOTE_STYlE = true
      settings.USE_DOUBLE_QUOTES = true
    })

    JavaScriptFormatterTest.setTempSettings(getProject(), JavascriptLanguage.INSTANCE, Consumer { settings ->
      settings.FORCE_QUOTE_STYlE = false
      settings.USE_DOUBLE_QUOTES = true
    })

    doTest("<script lang=\"ts\">\n" +
           "        import {Foo} from './bar'\n" +
           "\n" +
           "</script>",
           "<script lang=\"ts\">\n" +
           "    import {Foo} from \"./bar\"\n" +
           "\n" +
           "</script>", "vue")
  }
}
