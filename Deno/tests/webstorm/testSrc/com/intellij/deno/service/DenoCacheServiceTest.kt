package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.UseDeno
import com.intellij.deno.service.DenoServiceFixture.Companion.denoServiceFixture
import com.intellij.lang.typescript.service.TypeScriptServiceFixture
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.platform.lsp.tests.checkLspHighlighting
import com.intellij.platform.lsp.tests.checkLspHighlightingForData
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@TestApplication
class DenoCacheServiceTest {
  private val deno by denoServiceFixture()

  @BeforeEach
  fun setUp() {
    deno.deleteDenoCache()
  }

  @Test
  fun testModulePathCompletion() {
    val denoPath = DenoSettings.getService(deno.project).getDenoPath()
    deno.runSimpleCommandLine("$denoPath cache -r $stdPathUrl")
    DenoSettings.getService(deno.project).setUseDeno(UseDeno.ENABLE)

    deno.fixture.configureByText("main.ts", """
      import {join} from "https://deno.land/$stdVersion/<caret>path/mod.ts";
      
      join("1", "2");
    """.trimIndent())
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }

    val lookupElements = deno.fixture.completeBasic()
    assertEquals(stdPathUrl, lookupElements.firstOrNull()?.lookupString)
    TypeScriptServiceFixture.assertHasServiceItems(lookupElements, true)
  }

  @Test
  fun testCacheSimple() {
    deno.fixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = """https://deno.land/$stdVersion/testing/asserts.ts"""

    deno.fixture.addFileToProject(path, """
      |import { assertEquals } from <error>"$url<caret>"</error>;
      |assertEquals(1, 1);
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import { assertEquals } from "$url";
      |assertEquals(1, 1);
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheConfigUrl() {
    deno.fixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$stdPathUrl"
         }
      }
    """.trimIndent())

    val path = "./deno/src/main.ts"

    deno.fixture.addFileToProject(path, """
      |import {join} from <error>"#hello<caret>"</error>;
      |join("1", "2");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, stdPathUrl)

    val data = deno.createExpectedDataFromText("""
      |import {join} from "#hello";
      |join("1", "2");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheJsr() {
    deno.fixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "jsr:@luca/cases@^1.0.0"

    deno.fixture.addFileToProject(path, """
      |import { camelCase } from <error>"$url<caret>"</error>;
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import { camelCase } from "$url";
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheConfigJsr() {

    val url = "jsr:@luca/cases@^1.0.0"
    deno.fixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())


    val path = "./deno/src/main.ts"

    deno.fixture.addFileToProject(path, """
      |import {camelCase} from <error>"#hello<caret>"</error>;
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import {camelCase} from "#hello";
      |camelCase("hello");
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheConfigNpm() {
    val url = "npm:chalk@5"
    deno.fixture.addFileToProject("./deno/deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())


    val path = "./deno/src/main.ts"

    deno.fixture.addFileToProject(path, """
      |import {supportsColor} from <error>"#hello<caret>"</error>;
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import {supportsColor} from "#hello";
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheNpm() {
    deno.fixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "npm:chalk@5"

    deno.fixture.addFileToProject(path, """
      |import { supportsColor } from <error>"$url<caret>"</error>;
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import { supportsColor } from "$url";
      |console.log(supportsColor)
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }

  @Test
  fun testCacheNpmIndirectTypings() {
    deno.fixture.addFileToProject("deno/deno.json", "{}")
    val path = "./deno/src/main.ts"
    val url = "npm:marked@11.2.0"

    deno.fixture.addFileToProject(path, """
      |import { lexer } from <error>"$url<caret>"</error>;
      |lexer("")
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    val tmpFile = deno.fixture.configureFromTempProjectFile(path)
    runInEdtAndWait { deno.fixture.checkLspHighlighting() }
    deno.invokeCacheCommand(tmpFile.virtualFile, url)

    val data = deno.createExpectedDataFromText("""
      |import { lexer } from "$url";
      |lexer("")
      |<error>assertEqualsError123</error>();
    """.trimMargin())

    runInEdtAndWait { deno.fixture.checkLspHighlightingForData(data) }

    val resolved = runReadActionBlocking {
      val ref = deno.fixture.getReferenceAtCaretPosition()
      ref!!.resolve()
    }
    assertNotNull(resolved)
  }


  @Test
  fun testCleanCache() { //for debugging purposes
    deno.deleteDenoCache()
  }
}
