package org.intellij.plugins.markdown.reference

import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.testFramework.ResolveTestCase
import org.intellij.plugins.markdown.MarkdownTestingUtil
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl

class HeaderResolveTest : ResolveTestCase() {
  override fun getTestDataPath(): String = MarkdownTestingUtil.TEST_DATA_PATH + "/reference/linkDestination/headers/"

  private fun doTest() {
    val fileName = getTestName(true) + ".md"
    val reference = configureByFile(fileName)
    val resolve = reference.resolve()

    assertNotNull(resolve)
    assertTrue(resolve is MarkdownHeaderImpl)
  }

  fun testHeader1() {
    doTest()
  }

  fun testHeader2() {
    doTest()
  }

  fun testInBullet() {
    doTest()
  }

  fun testMultipleHeaders() {
    val fileName = getTestName(true) + ".md"
    val reference = configureByFile(fileName)
    assertInstanceOf(reference, PsiPolyVariantReference::class.java)

    val resolve = (reference as PsiPolyVariantReferenceBase<*>).multiResolve(false)
    assertTrue(resolve.size == 2)
  }
}