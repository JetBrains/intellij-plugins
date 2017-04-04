package com.intellij.coldFusion

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.sql.psi.SqlCommonKeywords
import org.junit.Test

/**
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionTest : CfmlCodeInsightFixtureTestCase() {

  override fun getBasePath() = "/injection"

  @Test
  fun testPlainSqlInjection() {
    prepare()
    val ref = getElementAtCaret()
    assertNotNull(ref)
    assertTrue(ref is LeafPsiElement)
    assertTrue((ref as LeafPsiElement).elementType == SqlCommonKeywords.SQL_SELECT)
  }

  @Test
  fun testWithCfqueryparam() {
    prepare()
    val ref = getElementAtCaret()
    assertNotNull(ref)
    assertTrue(ref is LeafPsiElement)
    assertTrue((ref as LeafPsiElement).elementType == SqlCommonKeywords.SQL_ORDER)
  }

  @Test
  fun testWithCfif() {
    prepare()
    val ref = getElementAtCaret()
    assertNotNull(ref)
    assertTrue(ref is LeafPsiElement)
    assertTrue((ref as LeafPsiElement).elementType == SqlCommonKeywords.SQL_JOIN)
  }

  private fun getElementAtCaret(): PsiElement {
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    assertNotNull(element)
    return element!!
  }

  private fun prepare() {
    myFixture.configureByFile("${getTestName(true)}.test.cfml")
  }

}