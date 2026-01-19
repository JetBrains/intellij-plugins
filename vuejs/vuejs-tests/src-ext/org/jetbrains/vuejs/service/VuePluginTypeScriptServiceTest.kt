// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.lang.typescript.compiler.TypeScriptServiceHolder
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase
import com.intellij.platform.testFramework.core.FileComparisonFailedError
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.VuePluginTypeScriptService
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.jetbrains.vuejs.options.getVueSettings
import java.io.File

class VuePluginTypeScriptServiceTest :
  TypeScriptServiceTestBase() {

  private val DEFAULT_VUE_MODULE: VueTestModule = VueTestModule.VUE_3_5_0

  override fun getBasePath(): String = vueRelativeTestDataPath() + "/ts-plugin"

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
    myFixture.configureByText(VueTsConfigFile.FILE_NAME, VueTsConfigFile.DEFAULT_TSCONFIG_CONTENT)
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

  private fun configureVueDependencies(
    vueModule: VueTestModule = DEFAULT_VUE_MODULE,
  ) {
    myFixture.configureVueDependencies(modules = arrayOf(vueModule, VueTestModule.VUE_TSCONFIG_0_8_1))
  }

  private fun doSimpleHighlightTest(
    vueModule: VueTestModule = DEFAULT_VUE_MODULE,
    fileName: String = "App",
    warnings: Boolean = false,
  ) {
    configureVueDependencies(vueModule)
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile("$fileName.$extension")
    checkHighlightingByOptions(warnings)
  }

  fun testBasicErrors() {
    doSimpleHighlightTest()
  }

  fun testSimpleVue() {
    doSimpleHighlightTest()
  }

  fun testEnableSuggestions() {
    doSimpleHighlightTest()
  }

  fun testAugmentedComponentCustomPropertiesWithOverrides() {
    doSimpleHighlightTest()
  }

  fun testAugmentedComponentCustomPropertiesWithOverrides__vapor() {
    doSimpleHighlightTest(VueTestModule.VUE_3_6_0)
  }

  fun testAugmentedComponentCustomPropertiesWithOverridesErrors() {
    doSimpleHighlightTest()
  }

  fun testAugmentedComponentCustomPropertiesWithOverridesErrors__vapor() {
    doSimpleHighlightTest(VueTestModule.VUE_3_6_0)
  }

  fun testSimpleRename() {
    configureVueDependencies()
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
    configureVueDependencies()
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
    configureVueDependencies()
    myFixture.configureByText("main.vue", """
      <script setup>
        import { ref } from 'vue'
        const message = re<caret>f('Hello World!')
      </script>
    """)

    myFixture.doHighlighting()
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
  }
}