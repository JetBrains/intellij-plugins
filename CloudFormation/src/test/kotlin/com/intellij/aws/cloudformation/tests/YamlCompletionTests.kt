package com.intellij.aws.cloudformation.tests

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import java.util.Arrays

class YamlCompletionTests : LightCodeInsightFixtureTestCase() {
  val predefinedAndECSCluster = (CloudFormationMetadataProvider.METADATA.predefinedParameters + "ECSCluster").toTypedArray()
  private fun Array<String>.withQuotes(quote: String): Array<String> = this.map { "$quote$it$quote" }.toTypedArray()

  fun testRefNoQuotes() = checkBasicCompletion("ref_no_quotes.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes() = checkBasicCompletion("ref_single_quotes.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes2() = checkBasicCompletion("ref_single_quotes_2.yaml", *predefinedAndECSCluster)
  fun testRefSingleQuotes3() = checkBasicCompletion("ref_single_quotes_3.yaml", *predefinedAndECSCluster.withQuotes("'"))
  fun testRefDoubleQuotes() = checkBasicCompletion("ref_double_quotes.yaml", *predefinedAndECSCluster.withQuotes("\""))
  fun testRefDoubleQuotes2() = checkBasicCompletion("ref_double_quotes_2.yaml", *predefinedAndECSCluster)
  fun testRefDoubleQuotes3() = checkBasicCompletion("ref_double_quotes_3.yaml", *predefinedAndECSCluster.withQuotes("\""))

  private fun checkBasicCompletion(fileName: String, vararg expectedElements: String) {
    myFixture.configureByFiles(fileName)
    myFixture.complete(CompletionType.BASIC, 1)
    val strings = myFixture.lookupElementStrings!!
    UsefulTestCase.assertSameElements(strings, Arrays.asList(*expectedElements))
  }

  override fun getTestDataPath(): String {
    return TestUtil.getTestDataPath("completion/yaml")
  }
}
