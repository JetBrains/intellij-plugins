package org.intellij.prisma.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.testFramework.UsefulTestCase
import junit.framework.TestCase

class PrismaJSCompletionTest : PrismaCompletionTestBase() {
  override fun getBasePath(): String = "/completion/js"

  fun testApiClientField() {
    val lookupElements = doDirTest()
    UsefulTestCase.assertSize(2, lookupElements)
    val userField = lookupElements.first()
    TestCase.assertEquals("user", userField.lookupString)
    val userType = userField.presentation?.typeText!!
    TestCase.assertTrue(userType.startsWith("Prisma.UserDelegate"))
  }

  private fun doDirTest(ext: String = "js"): Array<LookupElement> {
    val dir = myFixture.copyDirectoryToProject(getTestName(true), ".")
    val filename = "${getTestName(true)}.$ext"
    myFixture.configureFromExistingVirtualFile(dir.findChild(filename)!!)
    return myFixture.completeBasic() ?: emptyArray()
  }
}