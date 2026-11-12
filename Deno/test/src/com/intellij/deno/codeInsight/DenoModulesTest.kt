package com.intellij.deno.codeInsight

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.deno.DenoSettings
import com.intellij.deno.denoCodeInsightFixture
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.ui.UIUtil
import org.junit.jupiter.api.Test

@TestApplication
class DenoModulesTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val myFixture by denoCodeInsightFixture(projectFixture)

  @Test
  fun testSimpleAutoImport() {
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "Hell<caret>")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {Hello} from \"./hello.ts\";\n" +
                          "\n" +
                          "Hello")
  }

  @Test
  fun testSimpleFileName() {
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "import {} from './hel<caret>'")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {} from './hello.ts'")
  }

  @Test
  fun testSimpleMts() {
    myFixture.configureByText("hello.mts", "export class Hello")
    myFixture.configureByText("usage.ts", "import {} from './hel<caret>'")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {} from './hello.mts'")
  }

  @Test
  fun testSimpleMtsAutoImport() {
    myFixture.configureByText("hello.mts", "export class Hello")
    myFixture.configureByText("usage.ts", "Hell<caret>")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {Hello} from \"./hello.mts\";\n" +
                          "\n" +
                          "Hello")
  }

  @Test
  fun testImportMap() {
    myFixture.enableInspections(*JSDaemonAnalyzerFixture.defaultInspections())
    myFixture.configureByText("import_map.json", """
      {
        "imports": {
            "#hello": "./hello.ts"
         }
      }
    """.trimIndent())
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "import {Hello} from '#hello'")
    myFixture.testHighlighting()
  }

  @Test
  fun testImportMapNoResolve() {
    myFixture.enableInspections(*JSDaemonAnalyzerFixture.defaultInspections())
    val service = DenoSettings.getService(myFixture.project)
    val oldInit = service.getDenoInit()
    try {
      service.setDenoInit("{}")
      myFixture.configureByText("import_map.json", """
      {
        "imports": {
            "#hello": "./hello.ts"
         }
      }
    """.trimIndent())
      myFixture.configureByText("hello.ts", "export class Hello")
      myFixture.configureByText("usage.ts", "import {Hello} from '<error>#hello</error>'")
      runInEdtAndWait { UIUtil.dispatchAllInvocationEvents() }

      myFixture.testHighlighting()
    }
    finally {
      service.setDenoInit(oldInit)
    }
  }

  @Test
  fun testImportMapNested() {
    myFixture.enableInspections(*JSDaemonAnalyzerFixture.defaultInspections())
    myFixture.configureByText("import_map.json", """
      {
        "imports": {
            "#hello": "./hello.ts"
         }
      }
    """.trimIndent())
    myFixture.configureByText("hello.ts", "export class Hello")
    val file = myFixture.addFileToProject("subdir/usage.ts", "import {Hello} from '#hello'")
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    myFixture.testHighlighting()
  }

  @Test
  fun testDenoConfigNested() {
    myFixture.enableInspections(*JSDaemonAnalyzerFixture.defaultInspections())
    myFixture.configureByText("deno.json", """
      {
        "imports": {
            "#hello": "./hello.ts"
         }
      }
    """.trimIndent())
    myFixture.configureByText("hello.ts", "export class Hello")
    val file = myFixture.addFileToProject("subdir/usage.ts", "import {Hello} from '#hello'")
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    myFixture.testHighlighting()
  }
}