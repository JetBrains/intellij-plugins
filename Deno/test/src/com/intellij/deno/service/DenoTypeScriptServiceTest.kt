package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lang.javascript.typescript.TypeScriptFormatterTest
import com.intellij.lsp.checkLspHighlighting
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.ui.UIUtil

class DenoTypeScriptServiceTest : JSTempDirWithNodeInterpreterTest() {
  override fun setUp() {
    super.setUp()
    DenoSettings.getService(project).setUseDenoAndReload(true)
    (myFixture as CodeInsightTestFixtureImpl).canChangeDocumentDuringHighlighting(true)
  }

  fun testSimpleDeno() {
    // one error comes from JSReferenceChecker.reportUnresolvedReference, another one - from Deno LSP server
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error descr=\"Deno: Cannot find name 'Deno1'. Did you mean 'Deno'?\"><error descr=\"Unresolved variable or type Deno1\">Deno1</error></error>)")
    myFixture.checkLspHighlighting()
  }

  fun testDenoOpenCloseFile() {
    val file = myFixture.configureByText("bar.ts", "")
    myFixture.type("export class Hello {}\n" +
                   "<error descr=\"Deno: Cannot find name 'UnknownName'.\"><error descr=\"Unresolved variable or type UnknownName\">UnknownName</error></error>")
    close(file)

    myFixture.configureByText("foo.ts", "import {" +
                                        "<warning descr=\"Deno: `Hello` is never used\nIf this is intentional, alias it with an underscore like `Hello as _Hello`\">Hello</warning>, " +
                                        "<error descr=\"Cannot resolve symbol 'Goodbye'\"><error descr=\"Deno: Module '\\\"./bar.ts\\\"' has no exported member 'Goodbye'.\">Goodbye</error></error>} from './bar.ts';\n" +
                                        "<error descr=\"Deno: Cannot find name 'UnknownName'.\"><error descr=\"Unresolved variable or type UnknownName\">UnknownName</error></error>")
    myFixture.checkLspHighlighting()
    close(this.file)

    myFixture.openFileInEditor(file.virtualFile)
    myFixture.checkLspHighlighting()
  }

  fun testDenoSimpleRename() {
    val errorWithMarkup = "<error descr=\"Deno: Cannot find name 'UnknownName'.\"><error descr=\"Unresolved variable or type UnknownName\">UnknownName</error></error>"

    val fooFile = myFixture.addFileToProject("foo.ts", "export class Hello {}\n$errorWithMarkup")

    myFixture.configureByText("bar.ts", "import { Hello } from './foo.ts'\nconst _baz = new Hello<caret>()\n$errorWithMarkup")
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello } from './foo.ts'\nconst _baz = new Hello<caret>()\nUnknownName")

    myFixture.renameElementAtCaret("Hello1")
    WriteAction.run<Throwable> { myFixture.editor.document.setText(myFixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello1 } from './foo.ts'\nconst _baz = new Hello<caret>1()\nUnknownName")

    myFixture.renameElementAtCaret("Hello12")
    WriteAction.run<Throwable> { myFixture.editor.document.setText(myFixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello12 } from './foo.ts'\nconst _baz = new Hello<caret>12()\nUnknownName")

    myFixture.openFileInEditor(fooFile.virtualFile)
    myFixture.checkLspHighlighting()
    myFixture.checkResult("export class Hello12 {}\nUnknownName")
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
    Thread.sleep(2000)
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