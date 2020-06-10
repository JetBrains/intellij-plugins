package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.JSLanguageServiceBase
import com.intellij.lang.javascript.service.JSLanguageServiceProvider
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase
import com.intellij.testFramework.JSUnit38AssumeSupportRunner
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.containers.ContainerUtil
import org.junit.runner.RunWith

@RunWith(JSUnit38AssumeSupportRunner::class)
class DenoTypeScriptServiceTest : TypeScriptServiceTestBase() {
  var before = false

  override fun getService(): JSLanguageServiceBase {
    val services = JSLanguageServiceProvider.getLanguageServices(project)
    return ContainerUtil.find(services
    ) { el: JSLanguageService? -> el is DenoTypeScriptService } as JSLanguageServiceBase
  }

  override fun setUp() {
    super.setUp()
    val project = myFixture.project
    before = DenoSettings.getService(project).isUseDeno()
    DenoSettings.getService(project).setUseDeno(true)
  }

  override fun tearDown() {
    DenoSettings.getService(project).setUseDeno(before)
    super.tearDown()
  }

  fun testSimpleDeno() {
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error>Deno1</error>)")
    checkHighlightingByOptions(false)
  }

  fun testDenoOpenCloseFile() {
    val file = myFixture.configureByText("bar.ts", "export class Hello {}\n" +
                                                   "UnknownName")
    closeCurrentEditor()
    myFixture.configureByText("foo.ts", "import {Hello} from './bar.ts';\n<error>UnknownName</error>")
    checkHighlightingByOptions(false)
    closeCurrentEditor()
    myFixture.openFileInEditor(file.virtualFile)

    val document = DocumentImpl("export class Hello {}\n<error>UnknownName</error>")
    val data = ExpectedHighlightingData(document, false, false, false)
    data.init()
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
    myFixture.checkResult(document.text)
  }
}