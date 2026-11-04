package com.intellij.terraform.template

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/terraform/terraform-template/test-data/folding")
class TftplFoldingTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Test
  fun testIfFolding() {
    myFixture.testFolding("${myFixture.testDataPath}/if.tftpl")
  }

  @Test
  fun testForFolding() {
    myFixture.testFolding("${myFixture.testDataPath}/for.tftpl")
  }

  @Test
  fun testNestedFolding() {
    myFixture.testFolding("${myFixture.testDataPath}/nested.tftpl")
  }
}