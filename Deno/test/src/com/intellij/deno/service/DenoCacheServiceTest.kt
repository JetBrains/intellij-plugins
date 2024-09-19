package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.lang.typescript.service.TypeScriptServiceTestBase
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.checkLspHighlightingForData
import junit.framework.TestCase


class DenoCacheServiceTest : DenoServiceTestBase() {

  override fun setUp() {
    super.setUp()
    deleteDenoCache()
  }

  fun testModulePathCompletion() {
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

  fun testCacheSimple() {
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
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheConfigUrl() {
    myFixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$stdPathUrl"
         }
      }
    """.trimIndent())

    val path = "./deno/src/main.ts"

    myFixture.addFileToProject(path, """
      |import {join} from <error>"#hello<caret>"</error>;
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
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheJsr() {
    myFixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "jsr:@luca/cases@^1.0.0"

    myFixture.addFileToProject(path, """
      |import { camelCase } from <error>"$url<caret>"</error>;
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import { camelCase } from "$url";
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheConfigJsr() {

    val url = "jsr:@luca/cases@^1.0.0"
    myFixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())


    val path = "./deno/src/main.ts"

    myFixture.addFileToProject(path, """
      |import {camelCase} from <error>"#hello<caret>"</error>;
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import {camelCase} from "#hello";
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheConfigNpm() {
    val url = "npm:chalk@5"
    myFixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())


    val path = "./deno/src/main.ts"

    myFixture.addFileToProject(path, """
      |import {supportsColor} from <error>"#hello<caret>"</error>;
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import {supportsColor} from "#hello";
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheNpm() {
    myFixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "npm:chalk@5"

    myFixture.addFileToProject(path, """
      |import { supportsColor } from <error>"$url<caret>"</error>;
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import { supportsColor } from "$url";
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }

  fun testCacheNpmIndirectTypings() {
    myFixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "npm:marked@11.2.0"

    myFixture.addFileToProject(path, """
      |import { lexer } from <error>"$url<caret>"</error>;
      |lexer("")
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = myFixture.configureFromTempProjectFile(path)
    myFixture.checkLspHighlighting()
    invokeCacheCommand(tmpFile.virtualFile, url)

    val data = createExpectedDataFromText("""
      |import { lexer } from "$url";
      |lexer("")
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    myFixture.checkLspHighlightingForData(data)

    val ref = myFixture.getReferenceAtCaretPosition()
    val resolved = ref!!.resolve()
    assertNotNull(resolved)
  }


  fun testCleanCache() { //for debugging purposes
    deleteDenoCache()
  }
}
