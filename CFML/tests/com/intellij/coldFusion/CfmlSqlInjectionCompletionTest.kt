package com.intellij.coldFusion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.sql.completion.SqlCompletionTestCase
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.dialects.SqlLanguageDialect
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings
import com.intellij.sql.psi.SqlLanguage
import com.intellij.util.FileContentUtil
import java.util.*

/**
 * SQL injection completion tests for CFML without using data from datasource. Assuming that it is necessary to test only one of dialects, so let's take SQL92 for example.  Original tests and data are got from {@link com.intellij.sql.completion.AllSqlCompletionTest}
 *
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionCompletionTest : SqlCompletionTestCase(null) {

  val CFML_QUERY_TEMPLATE = "<cfquery><insert></cfquery>"

  override fun getTestDataPath(): String = CfmlTestUtil.BASE_TEST_DATA_PATH + basePath

  override fun getBasePath() = "/injection"

  fun testUpdate() =
    doTestVariants("create table ttt ( ); update <caret>", CompletionType.BASIC, 1, SqlCompletionTestCase.MatchType.CONTAINS, "ttt")

  fun testInsert() =
    doTestVariants("create table ttt ( ); insert into <caret>", CompletionType.BASIC, 1, SqlCompletionTestCase.MatchType.CONTAINS, "ttt")

  fun testSelect() =
    doTestVariants("create table ttt ( ccc int); select <caret>", CompletionType.BASIC, 1, SqlCompletionTestCase.MatchType.CONTAINS, "ccc")


  fun testFrom() {
    sqlSettings.KEYWORD_CASE = SqlCodeStyleSettings.TO_LOWER
    doTestVariants("select * <caret>", CompletionType.BASIC, 1, SqlCompletionTestCase.MatchType.CONTAINS, "from")
  }

  fun testSelectFromOrder() =
    doTestOrder("create table a(a1 int, a2 int);" +
                "create table b(b1 int, b2 int);" +
                "create table c(c1 int, c2 int);" +
                "select <caret> from c, b", CompletionType.BASIC,
                Arrays.asList("c1", "c2", "b1", "b2", "c", "b"), Arrays.asList("a"))


  // IDEA-90682
  fun testTableColumnListCompletion() =
    doTestVariants(
      "CREATE TABLE new_table (col INT, col2 INT, col3 INT DEFAULT 1, col4 INT DEFAULT 1, col5 INT); INSERT INTO new_table (<caret>)",
      CompletionType.BASIC, 0, SqlCompletionTestCase.MatchType.CONTAINS, "col, col2, col3, col4, col5", "col, col2, col5")


  fun testSmartCompletionInsert2() =
    doTestVariants("CREATE TABLE new_table (col INT, col2 INT, col3 INT, col4 INT, col5 INT); INSERT INTO new_table (id, <caret>)",
                   CompletionType.BASIC, 0, SqlCompletionTestCase.MatchType.INVERTED, "col, col2, col3, col4, col5")


  fun testRecoveredSelectCompletion() =
    doTestVariants("CREATE TABLE a (aa int);CREATE TABLE b (bb int);SELECT c.<caret> c.aa from a c",
                   SqlCompletionTestCase.MatchType.INVERTED, "bb")


  fun testFastConsumeBreakesCompletion() =
    doTestContains("SELECT (1 <caret>)", "IN")

  fun testNoExtraCompletion() =
    doTestVariants("CREATE TABLE t1(foo int); SELECT tt.f<caret>aq FROM t1 tt;", SqlCompletionTestCase.MatchType.INVERTED, "faq")

  override fun doTestVariants(sql: String,
                              type: CompletionType,
                              count: Int,
                              matchType: SqlCompletionTestCase.MatchType,
                              vararg variants: String): Unit {
    val dialect = SqlLanguage.INSTANCE.dialects.find { it.id == "SQL92" }
    SqlDialectMappings.getInstance(project).setMapping(null, dialect as SqlLanguageDialect)
    val file = myFixture.file
    if (file != null) FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
    println(javaClass.simpleName + "." + getTestName(true) + ": " + dialect.getID())
    configureByTemplate(CFML_QUERY_TEMPLATE, sql)
    doTestVariantsInner(type, count, matchType, *variants)
  }

  private fun configureByTemplate(cfmlTemplate: String, sqlInjection: String) {
    val CFML_FILE_TEXT = cfmlTemplate.replace("<insert>", sqlInjection)
    myFixture.configureByText("a.cfml", CFML_FILE_TEXT)
  }

}