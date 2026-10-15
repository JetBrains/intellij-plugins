package com.intellij.dts.ide

import com.intellij.dts.core.icons.DtsCoreIcons
import com.intellij.dts.dtsFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/ide/lineMark")
class DtsOverrideLineMarkerProviderTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  @Test
  fun `test no override`() = doTest(
    text = "&label {};",
    count = 0,
  )

  @Test
  fun `test new property`() = doTest(
    text = "&label { prop; };",
    count = 0,
  )

  @Test
  fun `test override`() = doTest(
    text = "&label { prop1; };",
    count = 1,
  )

  @Test
  fun `test multiple overrides`() = doTest(
    text = "&label { prop1;\nprop2; };",
    count = 2,
  )

  private fun doTest(text: String, count: Int) {
    dts.addFile("file.dtsi", dts.getFixture("ide/lineMark/File.dtsi"))

    dts.configureByText("""
            /include/ "file.dtsi"
            $text
        """)

    val gutter = myFixture.findAllGutters().filter { it.icon == DtsCoreIcons.OverrideProperty }

    assertThat(gutter).hasSize(count)
  }
}