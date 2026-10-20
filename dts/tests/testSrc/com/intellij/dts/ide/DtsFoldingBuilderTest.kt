package com.intellij.dts.ide

import com.intellij.dts.dtsFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/ide/folding")
class DtsFoldingBuilderTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  @Test
  fun `test node`() = doTest()

  @Test
  fun `test empty node`() = doTest()

  @Test
  fun `test root node`() = doTest()

  @Test
  fun `test empty root node`() = doTest()

  @Test
  fun `test nested node`() = doTest()

  private fun doTest() = myFixture.testFolding(dts.testFilePath)
}