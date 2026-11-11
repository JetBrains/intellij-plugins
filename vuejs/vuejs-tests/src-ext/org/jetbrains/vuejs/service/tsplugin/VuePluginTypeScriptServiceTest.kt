// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service.tsplugin

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.lang.typescript.service.TypeScriptServiceFixture.Companion.typeScriptServiceFixture
import com.intellij.openapi.application.readActionBlocking
import com.intellij.platform.testFramework.core.FileComparisonFailedError
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@TestApplication
class VuePluginTypeScriptServiceTest {
  private val vue by typeScriptServiceFixture(JSTempDirFixtureOptions.forBasePath(vueRelativeTestDataPath() + "/ts-plugin").withExtension("vue"))

  private val DEFAULT_VUE_MODULE: VueTestModule = VueTestModule.VUE_3_5_0

  @BeforeEach
  fun setUp() {
    vue.fixture.enableInspections(VueInspectionsProvider())
    vue.fixture.configureByText(VueTsConfigFile.FILE_NAME, VueTsConfigFile.DEFAULT_TSCONFIG_CONTENT)
  }

  private fun configureVueDependencies(
      vueModule: VueTestModule = DEFAULT_VUE_MODULE,
  ) {
    vue.fixture.configureVueDependencies(modules = arrayOf(vueModule, VueTestModule.VUE_TSCONFIG_0_9_1))
  }

  private fun doSimpleHighlightTest(
      vueModule: VueTestModule = DEFAULT_VUE_MODULE,
      fileName: String = "App",
      warnings: Boolean = false,
  ) {
    configureVueDependencies(vueModule)
    vue.fixture.copyDirectoryToProject(vue.getTestName(true), "")
    vue.fixture.configureFromTempProjectFile("$fileName.${vue.extension}")
    vue.checkHighlightingByOptions(warnings)
  }

  @Test
  fun testBasicErrors() {
    doSimpleHighlightTest()
  }

  @Test
  fun testSimpleVue() {
    doSimpleHighlightTest()
  }

  @Test
  fun testEnableSuggestions() {
    doSimpleHighlightTest()
  }

  @Test
  fun testAugmentedComponentCustomPropertiesWithOverrides() {
    doSimpleHighlightTest()
  }

  @Test
  fun testAugmentedComponentCustomPropertiesWithOverrides__vapor() {
    doSimpleHighlightTest(VueTestModule.VUE_3_6_0)
  }

  @Test
  fun testAugmentedComponentCustomPropertiesWithOverridesErrors() {
    doSimpleHighlightTest()
  }

  @Test
  fun testAugmentedComponentCustomPropertiesWithOverridesErrors__vapor() {
    doSimpleHighlightTest(VueTestModule.VUE_3_6_0)
  }

  @Test
  fun testSimpleRename() {
    configureVueDependencies()
    val fileToRename = vue.fixture.addFileToProject("Usage.vue", """
      <script setup lang="ts">
        console.log("test");
      </script>
      <template>text</template>
    """.trimIndent())

    vue.fixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      import Usage from './Us<caret>age.vue';
      console.log(Usage);
      </script>
      <template>
      </template>
    """.trimIndent())

    vue.fixture.checkHighlighting()
    runInEdtAndWait { vue.fixture.renameElement(fileToRename, "Usage2.vue") }

    //no errors
    vue.fixture.checkHighlighting()
  }

  @Test
  fun testBasicCompletion() {
    configureVueDependencies()
    vue.fixture.configureByText("main.vue", """
      <script setup>
        import { ref } from 'vue'
        const message = ref('Hello World!')
      </script>
      
      <template>
        <h1>{{ mess<caret> }}</h1>
      </template>
    """.trimIndent())

    vue.fixture.completeBasic()
    runInEdtAndWait { vue.fixture.type('\n') }

    vue.fixture.checkResult("""
      <script setup>
        import { ref } from 'vue'
        const message = ref('Hello World!')
      </script>
      
      <template>
        <h1>{{ message }}</h1>
      </template>
    """.trimIndent())
  }

  @Test
  fun testBasicDoc() {
    configureVueDependencies()
    vue.fixture.configureByText("main.vue", """
      <script setup>
        import { ref } from 'vue'
        const message = re<caret>f('Hello World!')
      </script>
    """)

    vue.fixture.doHighlighting()
    checkDoc()
  }

  private fun checkDoc() {
    val actualDocText = timeoutRunBlocking { readActionBlocking { JSAbstractDocumentationTest.getQuickDocumentationText(vue.fixture) } }
                        ?: throw AssertionError("No documentation found")
    val expectedDocFilePath = "${vue.testDataPath}/${vue.getTestName(false)}.expected.html"
    val expectedDocText = File(expectedDocFilePath).readText()
    if (expectedDocText != actualDocText) {
      throw FileComparisonFailedError(
          message = "Doc does not match",
          expected = expectedDocText,
          actual = actualDocText,
          expectedFilePath = expectedDocFilePath,
      )
    }
  }
}