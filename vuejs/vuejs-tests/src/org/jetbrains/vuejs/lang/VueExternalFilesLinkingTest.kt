// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.web.resolveReference
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueExternalFilesLinkingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/externalFiles"

  fun testLinkedTemplateVue() {
    doTest(targetFile = "main.vue")
  }

  fun testLinkedScriptVue() {
    doTest(mainFile = "main.vue")
  }

  fun testLinkedScriptTemplateVue() {
    doTest()
  }

  fun testLinkedScriptTemplateVueDecorators() {
    doTest()
  }

  fun testLinkedTemplateJSExportImport() {
    doTest()
  }

  fun testLinkedTemplateJSExportImportDecorators() {
    doTest()
  }

  fun testLinkedTemplateJSExportRequire() {
    doTest()
  }

  fun testLinkedTemplateJSExportRequireDecorators() {
    doTest()
  }

  fun testLinkedTemplateJSVueInitImport() {
    doTest()
  }

  fun testLinkedTemplateJSExportXTemplate() {
    doTest()

    myFixture.configureFromTempProjectFile("script.js")
    val element = myFixture.resolveReference("#b<caret>ar")
    assertEquals("template.html", element.containingFile.name)
    assertEquals("""<script type="text/x-template" id="bar">
    <div>
      {{ foo }}
    </div>
  </script>""", element.text)
  }

  private fun doTest(mainFile: String = "template.html", targetFile: String = "script.js") {
    val testName = getTestName(true)
    val targetText = if (testName.endsWith("Decorators")) "@Prop foo" else "\"foo\""
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.configureFromTempProjectFile(mainFile)
    val element = myFixture.resolveReference("{{ <caret>foo }}")
    assertEquals(targetFile, element.containingFile.name)
    assertEquals(targetText, element.context?.text)
  }

}
