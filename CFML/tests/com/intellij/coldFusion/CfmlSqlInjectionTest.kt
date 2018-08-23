package com.intellij.coldFusion

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
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
import kotlin.test.assertTrue

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
    myFixture.doHighlighting()
    waitCodeAnalysis(5_000)
    var document = myFixture.editor.document
    if (document is DocumentWindow) document = document.delegate
    val highlights = DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.ERROR, project)
    val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
    if (expectedErrorCount == 0) assertTrue("Highlighting errors should be empty, but this file has: $errors", errors.isNullOrEmpty())
    else TestCase.assertEquals("Highlight errors count should be ${expectedErrorCount}, got ${errors.size}", expectedErrorCount,
                               errors.size)
  }

  private fun waitCodeAnalysis(timeOutMillis: Int) {
    val stopAt = System.currentTimeMillis() + timeOutMillis
    while ( daemonCodeAnalyzer.isRunningOrPending) {
      assertTrue(System.currentTimeMillis() < stopAt, "waiting for daemon code analysis exceeded timeout $timeOutMillis(ms)")
      UIUtil.dispatchInvocationEvent()
    }
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
    val file: PsiFile = myFixture.file
    FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
  }

}