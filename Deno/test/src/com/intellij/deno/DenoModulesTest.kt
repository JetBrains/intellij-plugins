package com.intellij.deno

import com.intellij.codeInsight.completion.CompletionType

class DenoModulesTest : DenoTestBase() {
  
  fun testSimpleAutoImport() {
    myFixture.configureByText("hello.ts", "export class Hello")
    myFixture.configureByText("usage.ts", "Hell<caret>")
    myFixture.complete(CompletionType.BASIC)
    myFixture.checkResult("import {Hello} from \"./hello.ts\";\n" +
                          "\n" +
                          "Hello")
  }
}