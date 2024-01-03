package org.intellij.terraform.runtime

import com.intellij.icons.AllIcons
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.terraform.TerraformTestUtils

class TerraformRunLineMarkerContributorTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = TerraformTestUtils.getTestDataPath() + "/runtime"

  fun testSimpleLineMarker() {
    val file = myFixture.configureByFile("simple.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TerraformRunLineMarkerContributor().getInfo(it) }

    assertNotNull(info)
    assertEquals(AllIcons.RunConfigurations.TestState.Run, info?.icon)
  }

  fun testLineMarkerWithComment() {
    val file = myFixture.configureByFile("with_comment.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TerraformRunLineMarkerContributor().getInfo(it) }
    assertNotNull(info)
    assertEquals(AllIcons.RunConfigurations.TestState.Run, info?.icon)

    val gutter = myFixture.findGutter("with_comment.tf")
    assertNotNull(gutter)
    assertEquals(AllIcons.RunConfigurations.TestState.Run, gutter?.icon)
  }

  fun testLineMarkerWithWholeCommented() {
    val gutter = myFixture.findGutter("whole_commented.tf")
    assertNull(gutter)

    val gutters = myFixture.findAllGutters("whole_commented.tf")
    assertEmpty(gutters)
  }
}