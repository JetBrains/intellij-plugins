package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.checkLspHighlightingForData
import junit.framework.TestCase

private const val stdVersion = "std@0.187.0"
private const val stdPathUrl = "https://deno.land/$stdVersion/path/mod.ts"

class DenoCacheServiceTest : DenoServiceTestBase() {

  fun testDenoModulePathCompletion() {
    deleteDenoCache()

    val denoPath = DenoSettings.getService(project).getDenoPath()
    runSimpleCommandLine("$denoPath cache -r $stdPathUrl")
    DenoSettings.getService(project).setUseDeno(UseDeno.ENABLE)

    myFixture.configureByText("main.ts", """
      import {join} from "https://deno.land/$stdVersion/<caret>path/mod.ts";
      
      join("1", "2");
    """.trimIndent())
    myFixture.checkLspHighlighting()

    val lookupElements = myFixture.completeBasic()
    TestCase.assertEquals(stdPathUrl, lookupElements.firstOrNull()?.lookupString)
    TypeScriptServiceTestBase.assertHasServiceItems(lookupElements, true)
  }


  fun testDenoCacheCommand() {
    deleteDenoCache()

    myFixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = """https://deno.land/$stdVersion/testing/asserts.ts"""

    myFixture.addFileToProject(path, """
      |import { assertEquals } from <error>"$url<caret>"</error>;
      |assertEquals(1, 1);
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import { assertEquals } from "$url";
      |assertEquals(1, 1);
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    assertNotNull(ref?.resolve())
  }

  fun testConfigUrlCache() {
    deleteDenoCache()

    myFixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$stdPathUrl"
         }
      }
    """.trimIndent())

    val path = "./deno/src/main.ts"

    myFixture.addFileToProject(path, """
      |import {join} from <error>"<caret>#hello"</error>;
      |join("1", "2");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, stdPathUrl)

    val data = createExpectedDataFromText("""
      |import {join} from "#hello";
      |join("1", "2");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    assertNotNull(ref?.resolve())
  }

  fun testCleanCache() { //for debugging purposes
    deleteDenoCache()
  }
}
