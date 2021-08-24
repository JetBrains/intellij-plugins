package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.runner.RunWith

@RunWith(JUnit38AssumeSupportRunner::class)
class DenoTypeScriptServiceTest : JSTempDirWithNodeInterpreterTest() {
  var before = false

  override fun setUp() {
    super.setUp()
    before = DenoSettings.getService(project).isUseDeno()
    DenoSettings.getService(project).setUseDenoAndReload(true)
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  override fun tearDown() {
    DenoSettings.getService(project).setUseDenoAndReload(before)
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

  private fun closeCurrentEditor() {
    FileEditorManager.getInstance(project).closeFile(file.virtualFile)
  }
}