package com.intellij.deno.codeInsight

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoTestBase
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase

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
      myFixture.testHighlighting()
    }
    finally {
      service.setDenoInit(oldInit)
    }
  }
}