package com.intellij.coldFusion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.database.util.SqlDialects
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.sql.dialects.SqlDialectMappings
import com.intellij.sql.dialects.SqlLanguageDialect
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.FileContentUtil
import com.intellij.util.containers.ContainerUtil
import java.util.*

/**
 * SQL injection completion tests for CFML without using data from datasource. Assuming that it is necessary to test only one of dialects, so let's take SQL92 for example.  Original tests and data are got from {@link com.intellij.sql.completion.AllSqlCompletionTest}
 *
 * @author Sergey Karashevich
 */
class CfmlSqlInjectionCompletionTest : BasePlatformTestCase() {

  val CFML_QUERY_TEMPLATE = "<cfquery><insert></cfquery>"

  override fun getTestDataPath(): String = CfmlTestUtil.BASE_TEST_DATA_PATH + basePath

  override fun getBasePath() = "/injection"

  fun testUpdate() =
    doTestVariants("create table ttt ( ); update <caret>", CompletionType.BASIC, 1, true, "ttt")

  fun testInsert() =
    doTestVariants("create table ttt ( ); insert into <caret>", CompletionType.BASIC, 1, true, "ttt")

  fun testSelect() =
    doTestVariants("create table ttt ( ccc int); select <caret>", CompletionType.BASIC, 1, true, "ccc")

  // IDEA-90682
  fun testTableColumnListCompletion() =
    doTestVariants(
      "CREATE TABLE new_table (col INT, col2 INT, col3 INT DEFAULT 1, col4 INT DEFAULT 1, col5 INT); INSERT INTO new_table (<caret>)",
      CompletionType.BASIC, 0, true, "col, col2, col3, col4, col5", "col, col2, col5")


  fun testSmartCompletionInsert2() =
    doTestVariants("CREATE TABLE new_table (col INT, col2 INT, col3 INT, col4 INT, col5 INT); INSERT INTO new_table (id, <caret>)",
                   CompletionType.BASIC, 0, false, "col, col2, col3, col4, col5")


  fun testRecoveredSelectCompletion() =
    doTestVariants("CREATE TABLE a (aa int);CREATE TABLE b (bb int);SELECT c.<caret> c.aa from a c",
                   CompletionType.BASIC, 1, false, "bb")


  fun testFastConsumeBreaksCompletion() =
    doTestVariants("SELECT (1 <caret>)", CompletionType.BASIC, 1, true, "IN")

  fun testNoExtraCompletion() =
    doTestVariants("CREATE TABLE t1(foo int); SELECT tt.f<caret>aq FROM t1 tt;", CompletionType.BASIC, 1, false, "faq")

  fun doTestVariants(sql: String,
                              type: CompletionType,
                              count: Int,
                              contains: Boolean,
                              vararg variants: String) {
    val dialect = SqlDialects.findDialectById("SQL92")
    SqlDialectMappings.getInstance(project).setMapping(null, dialect as SqlLanguageDialect)
    val file = myFixture.file
    if (file != null) FileContentUtil.reparseFiles(project, listOf<VirtualFile>(file.virtualFile), false)
    println(javaClass.simpleName + "." + getTestName(true) + ": " + dialect.getID())
    configureByTemplate(CFML_QUERY_TEMPLATE, sql)
    myFixture.testVariantsInner(type, count, contains, variants)
  }

  private fun configureByTemplate(cfmlTemplate: String, sqlInjection: String) {
    val CFML_FILE_TEXT = cfmlTemplate.replace("<insert>", sqlInjection)
    myFixture.configureByText("a.cfml", CFML_FILE_TEXT)
  }

}

fun CodeInsightTestFixture.testVariantsInner(
  type: CompletionType,
  count: Int,
  contains: Boolean,
  variants: Array<out String>) {
  complete(type, count)
  val stringList = lookupElementStrings
  BasePlatformTestCase.assertNotNull(stringList)
  if (contains) {
    val missing = ArrayList(Arrays.asList(*variants))
    missing.removeAll(stringList!!)
    BasePlatformTestCase.assertTrue(
      "Missing variants: " + missing + "\nTop 10:\n" + ContainerUtil.getFirstItems(stringList, 10), missing.isEmpty())
  }
  else {
    val missing = ArrayList(Arrays.asList(*variants))
    missing.retainAll(stringList!!)
    BasePlatformTestCase.assertTrue("Present variants: $missing", missing.isEmpty())
  }
}
