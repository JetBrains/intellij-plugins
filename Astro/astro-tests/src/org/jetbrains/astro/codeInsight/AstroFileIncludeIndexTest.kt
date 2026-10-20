package org.jetbrains.astro.codeInsight

import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.impl.include.FileIncludeIndex
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class AstroFileIncludeIndexTest : AstroCodeInsightTestCase("codeInsight/fileIncludeIndex") {
  @Test
  fun testBasic() {
    val astroFile = myFixture.configureByFile("${astro.testName}.astro")
    val includes = runReadActionBlocking { FileIncludeIndex.getIncludes(astroFile.virtualFile, project).toList() }
    assertEquals(1, includes.size)
    assertEquals("foo.ts", includes[0].fileName)
  }
}
