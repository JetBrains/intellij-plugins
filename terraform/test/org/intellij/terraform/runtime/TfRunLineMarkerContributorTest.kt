package org.intellij.terraform.runtime

import com.intellij.icons.AllIcons
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.IconManager
import org.intellij.terraform.TerraformTestUtils

class TfRunLineMarkerContributorTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = TerraformTestUtils.getTestDataPath() + "/runtime"

  fun testSimpleLineMarker() {
    val file = myFixture.configureByFile("simple.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }

    assertNotNull(info)
    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info?.icon)
  }

  fun testLineMarkerWithComment() {
    val file = myFixture.configureByFile("with_comment.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    assertNotNull(info)
    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info?.icon)

    val gutter = myFixture.findGutter("with_comment.tf")
    assertNotNull(gutter)
    assertEquals(warnedRun, gutter?.icon)
  }

  fun testLineMarkerWithWholeCommented() {
    val gutter = myFixture.findGutter("whole_commented.tf")
    assertNull(gutter)

    val gutters = myFixture.findAllGutters("whole_commented.tf")
    assertEmpty(gutters)
  }
}