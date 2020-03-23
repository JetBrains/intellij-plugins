package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings

class VueFormatterTest : JavaScriptFormatterTestBase() {
  fun testTypeScriptWithEnforceCodeStyleSettings() {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) {
      val jsSettings = it.getCustomSettings(JSCodeStyleSettings::class.java)
      val typeScriptSettings = it.getCustomSettings(TypeScriptCodeStyleSettings::class.java)
      jsSettings.FORCE_SEMICOLON_STYLE = true
      jsSettings.USE_SEMICOLON_AFTER_STATEMENT = true
      jsSettings.FORCE_QUOTE_STYlE = true
      jsSettings.USE_DOUBLE_QUOTES = true

      typeScriptSettings.FORCE_SEMICOLON_STYLE = true
      typeScriptSettings.USE_SEMICOLON_AFTER_STATEMENT = false
      typeScriptSettings.FORCE_QUOTE_STYlE = true
      typeScriptSettings.USE_DOUBLE_QUOTES = false

      doTest("""<script lang="ts">
    import    {Foo} from './singleQuotes';
    import {Foo} from "./doubleQuotes"

        console.log('semicolon');
 console.log(   'no_semicolon')

</script>""",
             """<script lang="ts">
import {Foo} from './singleQuotes'
import {Foo} from './doubleQuotes'

console.log('semicolon')
console.log('no_semicolon')

</script>""", "vue")
    }
  }

  fun testScriptTagWithinTemplateTag() {
    doTest("""<template><script type="text/template"><div>
          </div>
</script></template>""",
           """<template>
    <script type="text/template">
        <div>
        </div>
    </script>
</template>""", "vue")
  }

}
