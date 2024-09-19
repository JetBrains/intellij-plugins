package com.intellij.deno.service

import com.intellij.deno.DenoSettings
import com.intellij.lang.javascript.modules.JSImportTestUtil

class DenoCacheImportsTest : DenoServiceTestBase() {
  override fun setUp() {
    super.setUp()
    deleteDenoCache()
  }

  fun testDepsCacheImportExact() {
    val settings = DenoSettings.getService(project)
    runSimpleCommandLine("${settings.getDenoPath()} cache -r $stdPathUrl")

    myFixture.addFileToProject("deno.json", "{}")
    val file = myFixture.addFileToProject("src/main.ts", """
      jo<caret>in("1", "2");
    """.trimIndent())
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {join} from \"https://deno.land/std@0.187.0/path/mod.ts\"'", false)
  }

  fun testDepsCacheConfigImportExact() {
    val settings = DenoSettings.getService(project)
    val denoPath = settings.getDenoPath()
    runSimpleCommandLine("$denoPath cache -r $stdPathUrl")

    myFixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "$stdPathUrl"
         }
      }
    """.trimIndent())

    val file = myFixture.addFileToProject("src/main.ts", """
      jo<caret>in("1", "2");
    """.trimIndent())
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {join} from \"#hello\"'", false)
  }

  fun testJsrCacheImportExact() {
    myFixture.addFileToProject("deno.json", "{}")
    val url = "jsr:@luca/cases@1.0.0"
    runSimpleCommandLine("${DenoSettings.getService(project).getDenoPath()} cache -r $url")

    val file = myFixture.addFileToProject("src/main.ts", """
      |camelC<caret>ase("hello");
    """.trimMargin())

    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {camelCase} from \"jsr:@luca/cases@1.0.0\"", false)
  }

  fun testJsrCacheConfigImportExact() {
    val url = "jsr:@luca/cases@1.0.0"
    myFixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "$url"
         }
      }
    """.trimIndent())
    runSimpleCommandLine("${DenoSettings.getService(project).getDenoPath()} cache -r $url")

    val file = myFixture.addFileToProject("src/main.ts", """
      |camelC<caret>ase("hello");
    """.trimMargin())

    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {camelCase} from \"#hello\"", false)
  }

  fun testNpmCacheImportExact() {
    myFixture.addFileToProject("deno.json", "{}")
    val url = "npm:chalk@5.0.0"
    runSimpleCommandLine("${DenoSettings.getService(project).getDenoPath()} cache -r $url")

    val file = myFixture.addFileToProject("src/main.ts", """
      |supportsCo<caret>lor;
    """.trimMargin())

    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {supportsColor} from \"npm:chalk@5.0.0\"'", false)
  }

  fun testNpmCacheConfigImportExact() {
    val url = "npm:chalk@5.0.0"
    myFixture.addFileToProject("deno.json", """
      {
        "imports": {
            "#hello": "npm:chalk@5.0.0"
         }
      }
    """.trimIndent())
    runSimpleCommandLine("${DenoSettings.getService(project).getDenoPath()} cache -r $url")

    val file = myFixture.addFileToProject("src/main.ts", """
      |supportsCo<caret>lor;
    """.trimMargin())

    myFixture.configureFromExistingVirtualFile(file.virtualFile)
    JSImportTestUtil.findAndInvokeIntentionAction(myFixture, "Insert 'import {supportsColor} from \"#hello\"", false)
  }
}