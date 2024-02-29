package org.intellij.terraform.runtime

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.RuntimeConfigurationException
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.IconManager
import org.intellij.terraform.TerraformTestUtils

class TerraformRunLineMarkerContributorTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = TerraformTestUtils.getTestDataPath() + "/runtime"

  fun testSimpleLineMarker() {
    val file = myFixture.configureByFile("simple.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TerraformRunLineMarkerContributor().getInfo(it) }

    assertNotNull(info)
    assertEquals(IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark), info?.icon)

    val configuration = getTerraformConfiguration(myFixture.elementAtCaret)
    assertEquals("plan", configuration.programParameters)
    assertEquals("/src", configuration.workingDirectory)
    assertTrue(configuration.envs.isEmpty())

    configuration.workingDirectory = ""
    assertThrows(RuntimeConfigurationException::class.java) { configuration.checkConfiguration() }
  }

  fun testLineMarkerWithComment() {
    val file = myFixture.configureByFile("with_comment.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TerraformRunLineMarkerContributor().getInfo(it) }
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

  private fun getTerraformConfiguration(psiElement: PsiElement): TerraformRunConfiguration {
    val configuration = ConfigurationContext(psiElement).configuration?.configuration
    assertInstanceOf(configuration, TerraformRunConfiguration::class.java)
    return configuration as TerraformRunConfiguration
  }
}