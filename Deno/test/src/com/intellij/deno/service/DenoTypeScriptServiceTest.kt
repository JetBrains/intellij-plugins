package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.modules.JSTempDirWithNodeInterpreterTest
import com.intellij.lsp.checkLspHighlighting
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl

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
    FileEditorManager.getInstance(project).closeFile(file.virtualFile)

    myFixture.configureByText("foo.ts", "import {" +
                                        "<warning descr=\"Deno: `Hello` is never used\nIf this is intentional, alias it with an underscore like `Hello as _Hello`\">Hello</warning>, " +
                                        "<error descr=\"Cannot resolve symbol 'Goodbye'\"><error descr=\"Deno: Module '\\\"./bar.ts\\\"' has no exported member 'Goodbye'.\">Goodbye</error></error>} from './bar.ts';\n" +
                                        "<error descr=\"Deno: Cannot find name 'UnknownName'.\"><error descr=\"Unresolved variable or type UnknownName\">UnknownName</error></error>")
    myFixture.checkLspHighlighting()
    FileEditorManager.getInstance(project).closeFile(this.file.virtualFile)

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
    val errorWithMarkup = "<error descr=\"Deno: Cannot find name 'UnknownName'.\"><error descr=\"Unresolved variable or type UnknownName\">UnknownName</error></error>"

    myFixture.configureByText("foo.ts", "import { Hello<caret> } from './subdir/bar.ts'\nconst _hi = new Hello()\n$errorWithMarkup")
    val bar = myFixture.addFileToProject("subdir/bar.ts", "export class Hello {}")
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello<caret> } from './subdir/bar.ts'\nconst _hi = new Hello()\nUnknownName")

    myFixture.renameElementAtCaret("Hello1")
    myFixture.renameElement(file, "foo1.ts")
    myFixture.renameElement(bar, "bar1.ts")
    WriteAction.run<Throwable> { myFixture.editor.document.setText(myFixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello<caret>1 } from './subdir/bar1.ts'\nconst _hi = new Hello1()\nUnknownName")

    myFixture.renameElementAtCaret("Hello2")
    FileDocumentManager.getInstance().saveAllDocuments()
    myFixture.moveFile("subdir/bar1.ts", "")
    FileDocumentManager.getInstance().saveAllDocuments()
    myFixture.moveFile("foo1.ts", "subdir")
    myFixture.renameElement(bar.containingDirectory, "subdir1")
    WriteAction.run<Throwable> { myFixture.editor.document.setText(myFixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
    myFixture.checkLspHighlighting()
    myFixture.checkResult("import { Hello<caret>2 } from '../bar1.ts'\nconst _hi = new Hello2()\nUnknownName")
  }
}