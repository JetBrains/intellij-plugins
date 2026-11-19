// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.polySymbols.testFramework.resolveReference
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueExternalFilesLinkingTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/externalFiles")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testLinkedTemplateVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest(targetFile = "main.vue")
  }

  @Test
  fun testLinkedScriptVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest(mainFile = "main.vue")
  }

  @Test
  fun testLinkedScriptTemplateVue(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedScriptTemplateVueDecorators(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSExportImport(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSExportImportDecorators(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSExportRequire(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSExportRequireDecorators(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSVueInitImport(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testLinkedTemplateJSExportXTemplate(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
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
    val testName = hybrid.getTestName(true)
    val targetSignature = if (testName.endsWith("Decorators")) "@Prop <caret>foo" else "\"<caret>foo\""
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureVueDependencies()
    myFixture.configureFromTempProjectFile(mainFile)
    myFixture.moveToOffsetBySignature("{{ <caret>foo }}")
    myFixture.checkGotoDeclaration("{{ <caret>foo }}", targetSignature, targetFile)
  }

}
