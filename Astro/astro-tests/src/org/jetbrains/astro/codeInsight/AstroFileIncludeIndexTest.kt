package org.jetbrains.astro.codeInsight

import com.intellij.psi.impl.include.FileIncludeIndex
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.Assert

class AstroFileIncludeIndexTest : AstroCodeInsightTestCase("codeInsight/fileIncludeIndex") {
  fun testBasic() {
    val astroFile = myFixture.configureByFile("$testName.astro")
    val includes = FileIncludeIndex.getIncludes(astroFile.virtualFile, project).toList()
    Assert.assertEquals(1, includes.size)
    Assert.assertEquals("foo.ts", includes[0].fileName)
  }
}
