// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase
import com.intellij.platform.testFramework.core.FileComparisonFailedError
import org.intellij.lang.annotations.Language
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VuePluginTypeScriptService
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File

class VuePluginTypeScriptServiceTest : TypeScriptServiceTestBase() {
  override fun getBasePath(): String = vueRelativeTestDataPath() + "/ts-plugin"

  @Language("JSON")
  private val tsconfig = """
    {
      "compilerOptions": {
        "moduleDetection": "force",
        "strict": true,
        "noUnusedLocals": true
      },
      "include": ["**/*.vue", "*.vue", "**/*.ts", "*.ts"]
    }
  """.trimIndent()

  override fun getExtension(): String {
    return "vue"
  }

  override val service: VuePluginTypeScriptService
    get() {
      return TypeScriptServiceHolder.getForFile(project, file.virtualFile) as VuePluginTypeScriptService
    }

  var oldTsPluginPreviewEnabled = false
  override fun setUp() {
    super.setUp()
    val vueSettings = getVueSettings(myFixture.project)
    oldTsPluginPreviewEnabled = vueSettings.tsPluginPreviewEnabled
    vueSettings.tsPluginPreviewEnabled = true
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByText("tsconfig.json", tsconfig)
  }

  override fun tearDown() {
    try {
      getVueSettings(myFixture.project).tsPluginPreviewEnabled = oldTsPluginPreviewEnabled
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  fun testBasicErrors() {
    myFixture.configureByText("main.vue", """
      <script lang="ts">
        const <error descr="TS6133: 'unusedVar' is declared but its value is never read.">unusedVar</error> = 213;
        const <error descr="TS2322: Type 'number' is not assignable to type 'string'."><error descr="TS6133: 'abc' is declared but its value is never read.">abc</error></error>: string = 213;
      </script>
    """.trimIndent())

    myFixture.testHighlighting()
  }

  fun testSimpleVue() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      let <error descr="TS2322: Type 'number' is not assignable to type 'string'."><error descr="TS6133: 'a' is declared but its value is never read.">a</error></error>: string = 1;
      
      function acceptNumber(num: number): number { return num; }
      
      acceptNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
      </script>
      
      <template>
        <div v-text="acceptNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)" />
        <div>{{acceptNumber(<error descr="Argument type true is not assignable to parameter type number"><error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error></error>)}}</div>
      </template>
    """.trimIndent())

    myFixture.checkHighlighting()
  }

  fun testEnableSuggestions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
        export function hello(<weak_warning descr="TS6133: 'p' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">p</weak_warning>: number, p2: number) {
          console.log(p2);
        }
        let <error descr="TS2322: Type 'number' is not assignable to type 'string'." textAttributesKey="ERRORS_ATTRIBUTES"><error descr="TS6133: 'a' is declared but its value is never read." textAttributesKey="ERRORS_ATTRIBUTES">a</error></error>: string = 1;
      </script>

      <template>
      </template>
    """.trimIndent())

    myFixture.checkHighlighting()
  }

  fun testSimpleRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    val fileToRename = myFixture.addFileToProject("Usage.vue", """
      <script setup lang="ts">
        console.log("test");
      </script>
      <template>text</template>
    """.trimIndent())

    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      import Usage from './Us<caret>age.vue';
      console.log(Usage);
      </script>
      <template>
      </template>
    """.trimIndent())

    myFixture.checkHighlighting()
    myFixture.renameElement(fileToRename, "Usage2.vue")

    //no errors
    myFixture.checkHighlighting()
  }

  fun testBasicCompletion() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByText("main.vue", """
      <script setup>
        import { ref } from 'vue'
        const message = ref('Hello World!')
      </script>
      
      <template>
        <h1>{{ mess<caret> }}</h1>
      </template>
    """.trimIndent())

    myFixture.completeBasic()
    myFixture.type('\n')

    myFixture.checkResult("""
      <script setup>
        import { ref } from 'vue'
        const message = ref('Hello World!')
      </script>
      
      <template>
        <h1>{{ message }}</h1>
      </template>
    """.trimIndent())
  }

  fun testBasicDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_4_0)
    myFixture.configureByText("main.vue", """
      <script setup>
        import { ref } from 'vue'
        const message = re<caret>f('Hello World!')
      </script>
    """)

    checkDoc()
  }

  private fun checkDoc() {
    val actualDocText = JSAbstractDocumentationTest.getQuickDocumentationText(myFixture)
                        ?: throw AssertionError("No documentation found")
    val expectedDocFilePath = "$testDataPath/${getTestName(false)}.expected.html"
    val expectedDocText = File(expectedDocFilePath).readText()
    if (expectedDocText != actualDocText) {
      throw FileComparisonFailedError(
        message = "Doc does not match",
        expected = expectedDocText,
        actual = actualDocText,
        expectedFilePath = expectedDocFilePath,
      )
    }
    assertEquals(File(expectedDocFilePath).readText(), actualDocText)
  }
}