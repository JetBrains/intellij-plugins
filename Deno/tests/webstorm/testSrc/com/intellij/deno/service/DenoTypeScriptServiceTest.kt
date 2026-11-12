package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.service.DenoServiceFixture.Companion.denoServiceFixture
import com.intellij.lang.typescript.compiler.preventTypeScriptServiceRestartOnContextChange
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.ui.UIUtil
import org.junit.jupiter.api.Test

@TestApplication
class DenoTypeScriptServiceTest {
  private val deno by denoServiceFixture()

  @Test
  fun testSimpleDeno() {
    DenoSettings.getService(deno.project).setUseDeno(UseDeno.ENABLE)
    // one error comes from JSReferenceChecker.reportUnresolvedReference, another one - from Deno LSP server
    deno.fixture.addFileToProject("deno.json", "{}")
    deno.fixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error descr=\"Deno: Cannot find name 'Deno1'. Did you mean 'Deno'?\">Deno1</error>)")
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
  }

  @Test
  fun testDenoOpenCloseFile() {
    DenoSettings.getService(deno.project).setUseDeno(UseDeno.ENABLE)
    deno.fixture.addFileToProject("deno.json", "{}")
    val file = deno.fixture.configureByText("bar.ts", "console.log(<error descr=\"Deno: Cannot find name 'UnknownName'.\">UnknownName</error>);\n")
    runInEdtAndWait {
      deno.fixture.type("export class Hello {}\n")
      FileEditorManager.getInstance(deno.project).closeFile(file.virtualFile)
    }

    deno.fixture.configureByText("foo.ts", "<weak_warning descr=\"Deno: All imports in import declaration are unused.\">import {<warning descr=\"Deno: `Hello` is never used\n" +
                                        "If this is intentional, alias it with an underscore like `Hello as _Hello`\">Hello</warning>, <error descr=\"Deno: Module '\\\"./bar.ts\\\"' has no exported member 'Goodbye'.\">Goodbye</error>} from './bar.ts';</weak_warning>\n" +
                                        "<error descr=\"Deno: Cannot find name 'UnknownName'.\">UnknownName</error>")
    runInEdtAndWait {
      deno.fixture.checkLspHighlighting()
      FileEditorManager.getInstance(deno.project).closeFile(deno.fixture.file.virtualFile)
    }

    runInEdtAndWait {
      deno.fixture.openFileInEditor(file.virtualFile)
      deno.fixture.checkLspHighlighting()
    }
  }

  @Test
  fun testDenoSimpleRename() {
    DenoSettings.getService(deno.project).setUseDeno(UseDeno.ENABLE)
    val errorWithMarkup = "<error descr=\"Deno: Cannot find name 'UnknownName'.\">UnknownName</error>"

    val fooFile = deno.fixture.addFileToProject("foo.ts", "export class Hello {}\n$errorWithMarkup")

    deno.fixture.configureByText("bar.ts",
                              "import { Hello } from './foo.ts'\nconst _baz = new Hello<caret>()\nconsole.log(_baz)\n$errorWithMarkup")
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.fixture.checkResult("import { Hello } from './foo.ts'\nconst _baz = new Hello<caret>()\nconsole.log(_baz)\nUnknownName")

    runInEdtAndWait {
      deno.fixture.renameElementAtCaret("Hello1")
      WriteAction.run<Throwable> { deno.fixture.editor.document.setText(deno.fixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
      deno.fixture.checkLspHighlighting()
    }
    deno.fixture.checkResult("import { Hello1 } from './foo.ts'\nconst _baz = new Hello<caret>1()\nconsole.log(_baz)\nUnknownName")

    runInEdtAndWait {
      deno.fixture.renameElementAtCaret("Hello12")
      WriteAction.run<Throwable> { deno.fixture.editor.document.setText(deno.fixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
      deno.fixture.checkLspHighlighting()
    }
    deno.fixture.checkResult("import { Hello12 } from './foo.ts'\nconst _baz = new Hello<caret>12()\nconsole.log(_baz)\nUnknownName")

    runInEdtAndWait {
      deno.fixture.openFileInEditor(fooFile.virtualFile)
      deno.fixture.checkLspHighlighting()
    }
    deno.fixture.checkResult("export class Hello12 {}\nUnknownName")
  }

  @Test
  fun testDenoFileRename() {
    deno.fixture.addFileToProject("deno.json", "{}")
    deno.fixture.addFileToProject("subdir/deno.json", "{}")
    val errorWithMarkup = "<error descr=\"Deno: Cannot find name 'UnknownName'.\">UnknownName</error>"

    deno.fixture.configureByText("foo.ts",
                              "import { Hello<caret> } from './subdir/bar.ts'\nconst _hi = new Hello()\nconsole.log(_hi)\n$errorWithMarkup")
    val bar = deno.fixture.addFileToProject("subdir/bar.ts", "export class Hello {}")
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.fixture.checkResult("import { Hello<caret> } from './subdir/bar.ts'\nconst _hi = new Hello()\nconsole.log(_hi)\nUnknownName")

    runInEdtAndWait {
      deno.fixture.renameElementAtCaret("Hello1")
      deno.fixture.renameElement(deno.fixture.file, "foo1.ts")
      deno.fixture.renameElement(bar, "bar1.ts")
      WriteAction.run<Throwable> { deno.fixture.editor.document.setText(deno.fixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
      UIUtil.dispatchAllInvocationEvents()

      deno.fixture.checkLspHighlighting()
    }
    deno.fixture.checkResult("import { Hello<caret>1 } from './subdir/bar1.ts'\nconst _hi = new Hello1()\nconsole.log(_hi)\nUnknownName")

    runInEdtAndWait {
      deno.fixture.renameElementAtCaret("Hello2")
      FileDocumentManager.getInstance().saveAllDocuments()
      deno.fixture.moveFile("subdir/bar1.ts", "")
      FileDocumentManager.getInstance().saveAllDocuments()
      deno.fixture.moveFile("foo1.ts", "subdir")

      preventTypeScriptServiceRestartOnContextChange { deno.fixture.renameElement(bar.containingDirectory, "subdir1") }
      WriteAction.run<Throwable> { deno.fixture.editor.document.setText(deno.fixture.editor.document.text.replace("UnknownName", errorWithMarkup)) }
      UIUtil.dispatchAllInvocationEvents()

      deno.fixture.checkLspHighlighting()
    }
    deno.fixture.checkResult("import { Hello<caret>2 } from '../bar1.ts'\nconst _hi = new Hello2()\nconsole.log(_hi)\nUnknownName")
  }

  @Test
  fun testDenoAutoConfiguredWhenDenoJsonFound() {
    deno.fixture.addFileToProject("deno/deno.json", "{\"compilerOptions\": {\"allowJs\": true}}")
    val path = "./deno/src/main.ts"
    deno.fixture.addFileToProject(path, "console.log(Deno); " +
                                     "console.log(<error descr=\"Deno: Cannot find name 'Deno1'. Did you mean 'Deno'?\">Deno1</error>);")
    deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
  }

  @Test
  fun testDenoAutoConfiguredWhenDenoJsoncFound() {
    deno.fixture.addFileToProject("deno/deno.jsonc", "{\"compilerOptions\": {\"allowJs\": true}}")
    val path = "./deno/src/main.ts"
    deno.fixture.addFileToProject(path, "console.log(Deno); " +
                                     "console.log(<error descr=\"Deno: Cannot find name 'Deno1'. Did you mean 'Deno'?\">Deno1</error>);")
    deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
  }

  @Test
  fun testDenoAutoConfiguredWhenDenoJsonNotFound() {
    val path = "./deno/src/main.ts"
    deno.fixture.addFileToProject(path, "console.log(<error descr=\"TS2304: Cannot find name 'Deno'.\">Deno</error>); " +
                                     "console.log(<error descr=\"TS2304: Cannot find name 'Deno1'.\">Deno1</error>)")
    deno.fixture.configureFromTempProjectFile(path)
    deno.fixture.testHighlighting()
  }

  @Test
  fun testDenoServiceFormatting() {
    DenoSettings.getService(deno.project).setDenoFormattingEnabled(true)
    deno.fixture.addFileToProject("deno/deno.json", "{\"fmt\": {\"singleQuote\": true}}")
    val path = "./deno/src/fmt.ts"
    deno.fixture.addFileToProject(path, """
        console.log(    
        
           "Deno")
    """)
    deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }

    val codeStyleManager = CodeStyleManager.getInstance(deno.project)
    WriteCommandAction.runWriteCommandAction(deno.project) { codeStyleManager.reformat(deno.fixture.file) }

    deno.fixture.checkResult("""
      console.log(
        'Deno',
      );
      
      """.trimIndent())
  }

  @Test
  fun testDenoServiceFormattingDisabled() {
    DenoSettings.getService(deno.project).setDenoFormattingEnabled(false)
    deno.fixture.addFileToProject("deno/deno.json", "{\"fmt\": {\"singleQuote\": true, \"noSemicolons\": true}}")
    val path = "./deno/src/fmt.ts"
    deno.fixture.addFileToProject(path, """
        console.log("Deno");""")
    deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }

    val codeStyleManager = CodeStyleManager.getInstance(deno.project)
    WriteCommandAction.runWriteCommandAction(deno.project) { codeStyleManager.reformat(deno.fixture.file) }

    // do not respect singleQuote and noSemicolons
    deno.fixture.checkResult("""console.log("Deno");""".trimIndent())
  }
}