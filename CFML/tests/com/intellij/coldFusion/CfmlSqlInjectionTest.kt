// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.database.Dbms
import com.intellij.database.psi.DbDataSource
import com.intellij.database.psi.DbPsiFacade
import com.intellij.database.psi.DbPsiFacadeImpl
import com.intellij.database.util.DbImplUtil
import com.intellij.database.util.DbSqlUtil
import com.intellij.database.util.SqlDialects
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.sql.database.SqlDataSourceImpl
import com.intellij.sql.database.SqlDataSourceManager
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.psi.SqlCommonKeywords
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.FileContentUtil
import com.intellij.util.ui.UIUtil
import junit.framework.TestCase
import org.junit.Test

/**
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionTest : CfmlCodeInsightFixtureTestCase() {

  override fun getBasePath() = "/injection"

  override fun setUp() {
    super.setUp()
    myFixture.createDataSource("Sqlite.create.ddl")
    myFixture.createDataSource("Sqlite2.create.ddl")
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

  @Test
  fun testWithFunctionalExpression() {
    prepareWithDatabase()
    doHighlightText()
  }

  private fun doHighlightText(expectedErrorCount: Int = 0) {
    myFixture.doHighlighting()
    var document = myFixture.editor.document
    if (document is DocumentWindow) document = document.delegate
    val highlights = DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.ERROR, project)
    val errors = highlights.filter { it.severity == HighlightSeverity.ERROR }
    if (expectedErrorCount == 0) assertTrue("Highlighting errors should be empty, but this file has: $errors", errors.isEmpty())
    else TestCase.assertEquals("Highlight errors count should be ${expectedErrorCount}, got ${errors.size}", expectedErrorCount,
                               errors.size)
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
    val dialect = SqlDialects.findDialectById("SQL92")
    SqlDialectMappings.getInstance(project).setMapping(myFixture.file.virtualFile, dialect)
    val file: PsiFile = myFixture.file
    FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
    myFixture.configureFromExistingVirtualFile(file.virtualFile)
  }

}

fun CodeInsightTestFixture.createDataSource(vararg ddlFiles: String): DbDataSource {
  val dialect = DbSqlUtil.getSqlDialect(Dbms.SQLITE)
  val urls = mutableListOf<String>()
  val project = project
  for (file in ddlFiles) {
    val virtualFile = copyFileToProject(file)
    SqlDialectMappings.getInstance(project).setMapping(virtualFile, dialect)
    urls.add(virtualFile.url)
  }

  val dataSource = SqlDataSourceImpl(dialect.dbms.name, project, null)
  dataSource.urls = urls

  val dbPsiFacade = DbPsiFacade.getInstance(project) as DbPsiFacadeImpl
  val manager = SqlDataSourceManager.getInstance(project)
  TestCase.assertNotNull(manager)

  manager.addDataSource(dataSource)
  Disposer.register(testRootDisposable, Disposable {
    manager.removeDataSource(dataSource)
    UIUtil.dispatchAllInvocationEvents()
    dbPsiFacade.flushUpdates()
    UIUtil.dispatchAllInvocationEvents()
  })
  val dataSourceElement = dbPsiFacade.dataSources.first { it.delegate == dataSource }
  TestCase.assertNotNull(dataSourceElement)

  DbImplUtil.invokeOnPooledThreadSync { dataSource.waitComputed() }
  UIUtil.dispatchAllInvocationEvents()
  dbPsiFacade.flushUpdates()
  UIUtil.dispatchAllInvocationEvents()

  val files = dataSource.roots
  TestCase.assertFalse(files.isEmpty())
  return dataSourceElement
}
