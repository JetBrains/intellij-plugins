package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.deno.service.DenoServiceFixture.Companion.denoServiceFixture
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class DenoCacheImportsTest {
  private val deno by denoServiceFixture()

  @BeforeEach
  fun setUp() {
    deno.deleteDenoCache()
  }

  @Test
  fun testDepsCacheImportExact() {
    val settings = DenoSettings.getService(deno.project)
    deno.runSimpleCommandLine("${settings.getDenoPath()} cache -r $stdPathUrl")

    deno.fixture.addFileToProject("deno.json", "{}")
    val file = deno.fixture.addFileToProject("src/main.ts", """
      jo<caret>in("1", "2");
    """.trimIndent())
    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {join} from \"https://deno.land/std@0.187.0/path/mod.ts\"'", false)
  }

  @Test
  fun testDepsCacheConfigImportExact() {
    val settings = DenoSettings.getService(deno.project)
    val denoPath = settings.getDenoPath()
    deno.runSimpleCommandLine("$denoPath cache -r $stdPathUrl")

    deno.fixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "$stdPathUrl"
         }
      }
    """.trimIndent())

    val file = deno.fixture.addFileToProject("src/main.ts", """
      jo<caret>in("1", "2");
    """.trimIndent())
    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {join} from \"#hello\"'", false)
  }

  @Test
  fun testJsrCacheImportExact() {
    deno.fixture.addFileToProject("deno.json", "{}")
    val url = "jsr:@luca/cases@1.0.0"
    deno.runSimpleCommandLine("${DenoSettings.getService(deno.project).getDenoPath()} cache -r $url")

    val file = deno.fixture.addFileToProject("src/main.ts", """
      |camelC<caret>ase("hello");
    """.trimMargin())

    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {camelCase} from \"jsr:@luca/cases@1.0.0\"", false)
  }

  @Test
  fun testJsrCacheConfigImportExact() {
    val url = "jsr:@luca/cases@1.0.0"
    deno.fixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())
    deno.runSimpleCommandLine("${DenoSettings.getService(deno.project).getDenoPath()} cache -r $url")

    val file = deno.fixture.addFileToProject("src/main.ts", """
      |camelC<caret>ase("hello");
    """.trimMargin())

    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {camelCase} from \"#hello\"", false)
  }

  @Test
  fun testNpmCacheImportExact() {
    deno.fixture.addFileToProject("deno.json", "{}")
    val url = "npm:chalk@5.0.0"
    deno.runSimpleCommandLine("${DenoSettings.getService(deno.project).getDenoPath()} cache -r $url")

    val file = deno.fixture.addFileToProject("src/main.ts", """
      |supportsCo<caret>lor;
    """.trimMargin())

    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {supportsColor} from \"npm:chalk@5.0.0\"'", false)
  }

  @Test
  fun testNpmCacheConfigImportExact() {
    val url = "npm:chalk@5.0.0"
    deno.fixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "npm:chalk@5.0.0"
         }
      }
    """.trimIndent())
    deno.runSimpleCommandLine("${DenoSettings.getService(deno.project).getDenoPath()} cache -r $url")

    val file = deno.fixture.addFileToProject("src/main.ts", """
      |supportsCo<caret>lor;
    """.trimMargin())

    deno.fixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(deno.fixture, "Insert 'import {supportsColor} from \"#hello\"", false)
  }
}