package com.intellij.coldFusion

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.sql.datasource.SqlDataSourceTestCase
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.dialects.sql92.Sql92Dialect
import com.intellij.sql.dialects.sqlite.SqliteDialect
import com.intellij.sql.psi.SqlCommonKeywords
import com.intellij.util.FileContentUtil
import com.intellij.util.containers.isNullOrEmpty
import com.intellij.util.ui.UIUtil
import junit.framework.TestCase
import org.junit.Test

/**
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionTest : CfmlCodeInsightFixtureTestCase() {

  override fun getBasePath() = "/injection"

  override fun setUp() {
    //this object needed just for data source creation
    super.setUp()
    object : SqlDataSourceTestCase() {
      fun create() {
        this.myFixture = this@CfmlSqlInjectionTest.myFixture
        createDataSource(SqliteDialect.INSTANCE, null, "Sqlite.create.ddl")
        createDataSource(SqliteDialect.INSTANCE, null, "Sqlite2.create.ddl")
      }
    }.create()
  }

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

  @Test
  fun testWithExpression() {
    prepareWithDatabase()
    doHighlightText()
  }

  @Test
  fun testWithCfElse() {
    prepareWithDatabase()
    doHighlightText()
  }

  @Test
  fun testWithCfElse2() {
    prepareWithDatabase()
    doHighlightText(expectedErrorCount = 2)
  }

  private fun doHighlightText(expectedErrorCount: Int = 0) {
    val highlights = mutableListOf<HighlightInfo>()
    val attemptsInitial = 3
    var attempts = attemptsInitial
    do {
      UIUtil.dispatchAllInvocationEvents()
      assertTrue("Unable to update highlighters in ${attemptsInitial} attempts", attempts-- > 0)
      highlights.addAll(myFixture.doHighlighting())
    }
    while (highlights.isEmpty())
    val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
    if (expectedErrorCount == 0) assertTrue("Highlighting errors should be empty, but this file has: $errors", errors.isNullOrEmpty())
    else TestCase.assertEquals("Highlight errors count should be ${expectedErrorCount}, got ${errors.size}", expectedErrorCount, errors.size)
  }

  private fun getElementAtCaret(): PsiElement {
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    assertNotNull(element)
    return element!!
  }

  private fun prepare() {
    myFixture.configureByFile("${getTestName(true)}.test.cfml")
  }

  private fun prepareWithDatabase() {
    prepare()
    val dialect = Sql92Dialect.INSTANCE
    SqlDialectMappings.getInstance(project).setMapping(myFixture.file.virtualFile, dialect)
    val file = myFixture.file
    if (file != null) FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
  }

}