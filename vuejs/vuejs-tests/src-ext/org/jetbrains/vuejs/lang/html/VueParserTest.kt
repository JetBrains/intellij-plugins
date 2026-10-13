// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html

import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.javascript.testFramework.web.JSHtmlParsingTest
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.BlockSupportImpl
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugins.postcss.PostCssEmbeddedTokenTypesProvider
import org.intellij.plugins.postcss.parser.PostCssParserDefinition
import org.jetbrains.vuejs.lang.expr.parser.VueJSParserDefinition
import org.jetbrains.vuejs.lang.html.lexer.VueEmbeddedContentSupport
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class VueParserTest : JSHtmlParsingTest(
  "vue",
  VueParserDefinition(),
  VueJSParserDefinition(),
  PostCssParserDefinition()
) {

  override fun getEmbeddedContentSupports(): List<HtmlEmbeddedContentSupport> {
    return super.getEmbeddedContentSupports() + VueEmbeddedContentSupport()
  }

  override fun getEmbeddedTokenTypesProviders(): List<EmbeddedTokenTypesProvider> {
    return super.getEmbeddedTokenTypesProviders() + PostCssEmbeddedTokenTypesProvider()
  }

  override fun checkResult(targetDataName: String, file: PsiFile) {
    super.checkResult(targetDataName, file)
    ensureReparsingConsistent(file)
  }

  private fun ensureReparsingConsistent(file: PsiFile) {
    WriteAction.runAndWait<RuntimeException> {
      DebugUtil.performPsiModification<RuntimeException>("ensureReparsingConsistent") {
        val fileText = file.text
        val diffLog = BlockSupportImpl().reparseRange(
          file, file.node, TextRange.allOf(fileText), fileText, EmptyProgressIndicator(), fileText)
        val event = diffLog.performActualPsiChange(file)
        assertTrue(event.changedElements.isEmpty(), "The reparse changed the elements: ${event.changedElements}")
      }
    }
  }

  private fun doTestVue(text: String) {
    doTest(text.trimIndent(), "test.vue")
  }

  override fun doTest(text: String, fileName: String) {
    super.doTest(if (fileName.endsWith(".vue")
                     || fileName.endsWith(VueFileElementType.INJECTED_FILE_SUFFIX)
                     || text.trimStart().startsWith("<!DOCTYPE")
                     || text.trimStart().startsWith("<?"))
                   text
                 else
                   "<template>\n$text\n</template>",
                 if (fileName.endsWith(VueFileElementType.INJECTED_FILE_SUFFIX))
                   fileName
                 else
                   "${fileName.substring(0, fileName.lastIndexOf('.'))}.vue")
  }

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/html/parser"

  @Test
  fun testScriptNoLang() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script>
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  @Test
  fun testScriptJs() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="js">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  @Test
  fun testScriptTs() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  @Test
  fun testScriptTypo() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="tss">
      class X {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  @Test
  fun testScriptsWithMixedLanguages() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts">
      export class X {}
      </script>
      <script setup>
      class XS {}
      </script>
      <template>
        <div v-if="class {}"></div>
      </template>
    """)
  }

  @Test
  fun testScriptTsTemplateFirst() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <div v-if="class {}"></div>
      </template>
      <script lang="ts">
      class X {}
      </script>
    """)
  }

  @Test
  fun testScriptTsNoContent() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <div v-if="class {}"></div>
      </template>
      <script lang="ts" />
    """)
  }

  @Test
  fun testFirstEverScriptInTemplate() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <template>
        <script></script>
        <div v-if="class {}">This is buggy</div>
      </template>
      <script lang="ts">
      export class X {}
      </script>
      <script setup>
      class XS {}
      </script>
    """)
  }

  @Test
  fun testVueInnerScriptTagTS() {
    // classes have a different element type between JS & TS contexts
    doTestVue("""
      <script lang="ts"></script>
      <template>
        <script type="text/x-template" id="foo">
          <div v-if="class {}"></div>
          <script>class {}</script>
        </script>
      </template>
    """)
  }

  @Test
  fun testScriptStrangeLang1() {
    doTestVue("""
      <script lang="html">
        <div v-if="true"></div>
      </script>
    """)
  }

  @Test
  fun testScriptStrangeLang2() {
    doTestVue("""
      <script lang="template">
        <div v-if="true"></div>
      </script>
    """)
  }

  @Test
  fun testVue2FilterJs() {
    doTestVue("""
      <script lang="js"></script>
      <template>
        <div v-bind:title="1 | fooFilter"></div>
      </template>
    """)
  }

  @Test
  fun testVue2FilterTs() {
    doTestVue("""
      <script lang="ts"></script>
      <template>
        <div v-bind:title="1 | fooFilter"></div>
      </template>
    """)
  }

  @Test
  fun testVueInnerScriptTag() {
    doTestVue("""
      <template>
        <script type="text/template"><div></div></script>
      </template>
    """)
  }

  @Test
  fun testVueInnerScriptTag2() {
    doTestVue("""
      <template>
        <script type="text/template"><div></script></div>
      </template>
    """)
  }

  @Test
  fun testVueInnerScriptTag3() {
    doTestVue("""
      <template>
        <script type="text/x-template" id="foo">
          <div :foo="some - script()"></div>
        </script>
      </template>
    """)
  }

  @Test
  fun testSlotName() {
    doTestVue("""
      <template>
        <slot name='foo'></slot>
      </template>
    """)
  }

  @Test
  fun testSlotName2() {
    doTestVue("""
      <template>
        <script type="text/x-template" id="foo">
          <slot name='foo'></slot>
        </script>
      </template>
    """)
  }

  @Test
  fun testPropBinding() {
    doTestVue("""
      <template>
        <a v-bind:href="url"></a>
        <a :href="url"></a>
        <a :href=url></a>
        <a href="https://foo.bar"></a>
      </template>
    """)
  }

  @Test
  fun testEventBinding() {
    doTestVue("""
      <template>
        <a v-on:click="doSomething"></a>
        <a @click="doSomething"></a>
        <a onclick="doSomething()"></a>
      </template>
    """)
  }

  @Test
  fun testEmptyBinding() {
    doTestVue("""
      <template>
        <a @click=""></a>
        <a :href=""></a>
      </template>
    """)
  }

  @Test
  fun testVFor() {
    doTestVue("""
      <template>
        <div v-for="(item, index) in items">
          {{ parentMessage }} - {{ index }} - {{ item.message }}
        </div>
      </template>
    """)
  }

  @Test
  fun testCustomDirective() {
    doTestVue("""
      <template>
        <a v-foo:smth="{a: 12, b: true}"></a>
      </template>
    """)
  }

  @Test
  fun testInterpolationParsing() {
    doTest("""
      {{foo.bar}}
      <div v-bind:foo="{{bar}}" foo="{{bar}}" foo2='{{bar}}'>
        {{foo.bar}}
      </div>
    """.trimIndent(), "test.{{.}}${VueFileElementType.INJECTED_FILE_SUFFIX}")
  }

  @Test
  fun testCustomInterpolationParsing() {
    doTest("""
      {%foo.bar%}
      <div v-bind:foo="{{bar}}" foo="{{bar}}" foo2='{%bar%}'>
        {{foo.bar}}
      </div>
    """.trimIndent(), "test.{%.%}${VueFileElementType.INJECTED_FILE_SUFFIX}")
  }

  @Test
  fun testSrcAttribute() {
    doTestVue("""
      <template src="foo"></template>
      <script src="foo"></script>
      <style src="foo"></style>
      <img src="foo">
      <template>
        <template src="foo"></template>
        <script src="foo"></script>
        <style src="foo"></style>
        <img src="foo">
      </template>
    """.trimIndent())
  }

  @Test
  fun testLangAttribute() {
    doTestVue("""
      <template><div lang="ts"><span></span></div></template>
      <template lang="html"><div lang="ts"><span></span></div></template>
    """.trimIndent())
  }

  @Test
  fun testDivNestedUnderP() {
    doTestVue("<template><p><div></div></p></template>")
  }

  @Test
  fun testScriptSetupGeneric() {
    doTestVue("""
      <script setup lang="ts" generic="Clearable extends boolean, ValueType extends string | number | null | undefined">
      </script>
    """.trimIndent())
  }

  @Test
  fun testScriptSetupGenericBroken() {
    doTestVue("""
      <script setup lang="ts" generic="Clearable boolean, , ValueType extends string || number, ||">
      </script>
    """.trimIndent())
  }

  @Test
  fun testScriptSetupGenericJS() {
    doTestVue("""
      <script setup generic="Clearable extends boolean">
      </script>
    """.trimIndent())
  }

  @Test
  fun testTitleVue() {
    doTestVue("""
      <head><title>This is <std>title</std></title>
      <div @click="clicked()"><Title>This is <custom>title</custom></Title></div>
    """.trimIndent())
  }

  @Test
  fun testTitleHtml() {
    super.doTest("""
      <head><title>This is <std>title</std></title>
      <div @click="clicked()"><Title>This is <custom>title</custom></Title></div>
    """.trimIndent(), "test.html")
  }

  @Test
  fun testStyleReparse() {
    doReparseTest("<style scoped></styl\n", "<style scoped></style\n")
    doReparseTest("<style scoped></styl\n", "<style scoped></style><div\n")
    doReparseTest("<style scoped>.foo { }</styl\n", "<style scoped>.foo { }</style\n")
    doReparseTest("<style scoped>.foo { }</styl\n", "<style scoped>.foo { }</style><div\n")
  }

  @Test
  fun testJsxReparse() {
    doReparseTest("""
      <script lang="jsx">
      const dropdown = () => (<div style>a</div>)
      </script>
    """.trimIndent(), """
      <script lang="jsx">
      const dropdown = () => (<div style="foo">a</div>)
      </script>
    """.trimIndent())
  }

  @Test
  fun testLangReparse() {
    val baseText = """
      <script lang="js">
      export class X1 {}
      </script>

      <script setup>
      class X2 {}
      </script>
      
      <template>
        <div v-text="class {}"></div>
      </template>
    """.trimIndent()
    doReparseTest(baseText, baseText.replace("js", "ts"))
  }

  @Test
  fun testDeepWithSelectors() {
    doTestVue("""
      <template>
      </template>
      <style scoped>
      .a :deep(.b > .c) {
        color: red;
      }
      </style>
    """.trimIndent())
  }
}