package com.intellij.deno.service

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.javascript.typescript.TypeScriptFormatterTest
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.impl.CoreProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.ui.UIUtil
import junit.framework.TestCase
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
    close(file)
    myFixture.configureByText("foo.ts", "import {Hello} from './bar.ts';\n<error>UnknownName</error>")
    checkHighlightingByOptions(false)
    close(this.file)
    myFixture.openFileInEditor(file.virtualFile)

    val document = DocumentImpl("export class Hello {}\n<error>UnknownName</error>")
    val data = ExpectedHighlightingData(document, false, false, false)
    data.init()
    (myFixture as CodeInsightTestFixtureImpl).collectAndCheckHighlighting(data)
    myFixture.checkResult(document.text)
  }

  fun _testDenoSimpleRename() {
    val foo = myFixture.configureByText("foo.ts", "import { Hello } from './bar.ts'\n" +
                                                  "const _hi<caret> = new Hello()")
    myFixture.configureByText("bar.ts", "export class Hell<caret>o {}")
    checkHighlightingByOptions(false)
    myFixture.renameElementAtCaret("Fuzzy")
    checkHighlightingByOptions(false)
    myFixture.openFileInEditor(foo.virtualFile)
    val elem = myFixture.elementAtCaret
    val target = DocumentationManager.getInstance(project).findTargetElement(editor, file, elem)!!
    val service = DenoTypeScriptService.getInstance(project)
    CoreProgressManager.getInstance().executeNonCancelableSection {
      TestCase.assertEquals("const _hi: Fuzzy", service.quickInfo(target))
    }
  }

  fun testDenoFileRename() {
    myFixture.configureByText("foo.ts", "import { Hello } from './bar.ts'\nconst hi = new Hello()")
    val bar = myFixture.configureByText("bar.ts", "export class Hello {}")
    checkHighlightingByOptions(false)
    myFixture.renameElement(bar, "hello.ts")
    checkHighlightingByOptions(false)
  }

  fun testDenoReformat() {
    TypeScriptFormatterTest.setTempSettings(project) {
      it.FORCE_SEMICOLON_STYLE = true
    }
    myFixture.configureByText("foo.ts", "export class Foo {}")
    myFixture.configureByText("bar.ts", "export class Bar {}")
    myFixture.configureByText("test.ts", """
      import {  Foo  } from './foo.ts'
      import {  Bar  } from './bar.ts'
      console.log(Foo)
      console.log(Bar)
      console.log(<error>Bar1</error>)
    """.trimIndent())
    checkHighlightingByOptions(false)
    CommandProcessor.getInstance().executeCommand(project, {
      WriteAction.run<RuntimeException> {
        CodeStyleManager.getInstance(project).reformat(myFixture.file)
      }
    }, "write", null)
    UIUtil.dispatchAllInvocationEvents()
    //have to wait for the annotations, because diagnostics are async
    JSTestUtils.waitForConditionWithTimeout({ false }, 2000)
    myFixture.doHighlighting()
    JSDaemonAnalyzerLightTestCase.checkHighlightingByText(myFixture, """
      import {Foo} from './foo.ts';
      import {Bar} from './bar.ts';

      console.log(Foo);
      console.log(Bar);
      console.log(<error>Bar1</error>);
    """.trimIndent(), true)
  }

  private fun close(file: PsiFile) {
    FileEditorManager.getInstance(project).closeFile(file.virtualFile)
  }
}