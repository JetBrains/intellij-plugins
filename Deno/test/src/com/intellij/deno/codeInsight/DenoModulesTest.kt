package com.intellij.deno.codeInsight

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoTestBase
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.util.ui.UIUtil

class DenoModulesTest : DenoTestBase() {

  fun testSimpleAutoImport() {
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "Hell<caret>")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {Hello} from \"./hello.ts\";\n" +
                          "\n" +
                          "Hello")
  }

  fun testSimpleFileName() {
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "import {} from './hel<caret>'")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {} from './hello.ts'")
  }

  fun testSimpleMts() {
    myFixture.configureByText("hello.mts", "export class Hello")
    myFixture.configureByText("usage.ts", "import {} from './hel<caret>'")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {} from './hello.mts'")
  }

  fun testSimpleMtsAutoImport() {
    myFixture.configureByText("hello.mts", "export class Hello")
    myFixture.configureByText("usage.ts", "Hell<caret>")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {Hello} from \"./hello.mts\";\n" +
                          "\n" +
                          "Hello")
  }

  fun testImportMap() {
    myFixture.enableInspections(*JSDaemonAnalyzerLightTestCase.defaultInspections())
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

  fun testImportMapNoResolve() {
    myFixture.enableInspections(*JSDaemonAnalyzerLightTestCase.defaultInspections())
    val service = DenoSettings.getService(project)
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
      UIUtil.dispatchAllInvocationEvents()

      myFixture.testHighlighting()
    }
    finally {
      service.setDenoInit(oldInit)
    }
  }

  fun testImportMapNested() {
    myFixture.enableInspections(*JSDaemonAnalyzerLightTestCase.defaultInspections())
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

  fun testDenoConfigNested() {
    myFixture.enableInspections(*JSDaemonAnalyzerLightTestCase.defaultInspections())
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