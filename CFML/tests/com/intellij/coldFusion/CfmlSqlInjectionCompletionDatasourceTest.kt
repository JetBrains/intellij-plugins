package com.intellij.coldFusion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.database.util.SqlDialects
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.dialects.SqlLanguageDialect
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.FileContentUtil

/**
 * SQL injection completion tests for CFML without using data from datasource. Assuming that it is necessary to test only one of dialects, so let's take SQL92 for example.  Original tests and data are got from {@link com.intellij.sql.completion.AllSqlCompletionTest}
 *
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionCompletionDatasourceTest : BasePlatformTestCase() {

  val CFML_QUERY_TEMPLATE = "<cfquery><insert></cfquery>"

  override fun getTestDataPath(): String = CfmlTestUtil.BASE_TEST_DATA_PATH + basePath

  override fun getBasePath() = "/injection"

  override fun setUp() {
    super.setUp()
    myFixture.createDataSource("Sqlite.create.ddl")
  }

  fun testCompletion() {
    myDoTestVariants("select * from <caret>", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql01() {
    myDoTestVariants("select * from autogenerated_author where name=<cfqueryparam> and <caret>", CompletionType.BASIC, 1, true, "author_id")
  }

  fun testCompletionWihSplittedSql02() {
    myDoTestVariants("select * from autogenerated_author where name=<cfqueryparam value=\"some value is here\"> and <caret>", CompletionType.BASIC, 1, true, "author_id")
  }

  fun testCompletionWihSplittedSql03() {
    myDoTestVariants("select * from <caret> where name=<cfqueryparam> and author_id='some_id'", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql04() {
    myDoTestVariants("select * from <caret> where name=<cfqueryparam value=\"some value is here\"> and author_id='some_id'", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql05() {
    myDoTestVariants("select * from <caret> where name=<cfqueryparam value=\"some value is here\"> and author_id='some_id'", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql06() {
    myDoTestVariants("select * from <caret> WHERE name=<cfqueryparam> ", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql07() {
    myDoTestVariants("select * <caret> autogenerated_author WHERE name=<cfqueryparam> ", CompletionType.BASIC, 1, true, "FROM")
  }

  fun testCompletionWihSplittedSql08() {
    myDoTestVariants("select * from autogenerated_author where name=<cfif>'one variant'<cfelse>'second variant'</cfif> AND <caret> ", CompletionType.BASIC, 1, true, "author_id")
  }

  fun testCompletionWihSplittedSql09() {
    myDoTestVariants("select * from <caret> where name=<cfif>'one variant'<cfelse>'second variant'</cfif> AND author_id='frefe' ", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql10() {
    myDoTestVariants("select * <caret> autogenerated_author where name=<cfif>'one variant'<cfelse>'second variant'</cfif> AND author_id='frefe' ", CompletionType.BASIC, 1, true, "FROM")
  }

  fun testCompletionWihSplittedSql11() {
    myDoTestVariants("select * from <caret> where name=<cfif>'one variant'</cfif> AND author_id='frefe' ", CompletionType.BASIC, 1, true, "autogenerated_author")
  }

  fun testCompletionWihSplittedSql12() {
    myDoTestVariants("select * from autogenerated_author where name=<cfif>'one variant'</cfif> AND <caret> ", CompletionType.BASIC, 1, true, "author_id")
  }

  fun testCompletionWihSplittedSql13() {
    myDoTestVariants("select * <caret> autogenerated_author where name=<cfif>'one variant'<cfelse>'second variant'</cfif> ", CompletionType.BASIC, 1, true, "FROM")
  }

  private fun myDoTestVariants(sql: String,
                               type: CompletionType,
                               count: Int,
                               contains: Boolean,
                               vararg variants: String) {
    val dialect = SqlDialects.findDialectById("SQLite")
    SqlDialectMappings.getInstance(project).setMapping(null, dialect as SqlLanguageDialect)
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    val file = myFixture.file
    println(javaClass.simpleName + "." + getTestName(true) + ": " + dialect.getID())
    val fileText = CFML_QUERY_TEMPLATE.replace("<insert>", sql)
    myFixture.configureByText("a.cfml", fileText)
    if (file != null) FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
    myFixture.testVariantsInner(type, count, contains, variants)
  }

}
